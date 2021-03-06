/*
 *******************************************************************************************
 * Copyright (C) 2020 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *   find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *   you have going on right now! We'd love to be able to help out! Shoot us 
 *   any questions you may have, all our contact info should be on our website
 *   (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *   Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *   if you would consider donating to our club to help further STEM education.
 */

package frc.robot.ShooterControl;

import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANSparkMaxLowLevel.PeriodicFrame;

import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Annotations.Signal;
import frc.lib.SignalMath.AveragingFilter;
import frc.robot.RobotConstants;

/**
 * Add your docs here.
 */
public class RealShooterControl extends ShooterControl {

    private final int SPOOLUP_PID_SLOT_ID = 0;
    
    @Signal
    double adjustedSetpointRPM = 0;
    double prevAdjustedSetpointRPM = 0;
    boolean setpointChanged = false;

    @Signal
    boolean underLoad = false;
    @Signal
    ShooterCtrlMode currentStateShooter;
    ShooterCtrlMode previousStateShooter;
    @Signal
    double shooterActualSpeed_rpm; //Arbitrated shooter wheel speed
    double shooterAtSteadyStateDebounceCounter;
    @Signal
    double shooterHoldCmdVoltage = 0;

    @Signal
    double shooterSpeedErrorAbs = 0;
    double shooterSpeedErrorPrev = 0;
    double shooterSpeedErrorDeriv = 0;
    AveragingFilter shooterSpeedErrorDerivFilter;

    int shotCount=0;

    Calibration shooterMaxHoldErrorRPM;
    Calibration shooterMaxErrorRPM;
    Calibration shooterSpoolUpSteadyStateDbnc;
    Calibration accelerateToStabilizeThreshRPM;
    Calibration holdToShootErrThreshRPM;
    Calibration EjectSpeed;
    Calibration shooterShootVoltage;

    Calibration shooterMotorP_spoolup;
    Calibration shooterMotorI_spoolup;
    Calibration shooterMotorD_spoolup;
    Calibration shooterMotorF_spoolup;
    Calibration shooterMotorIZone_spoolup;
    Calibration percentSpeed;

    Calibration loadedDebounceRPMCal;

    boolean calsUpdated;

    CANSparkMax shooterMotor1; //Master
    CANSparkMax shooterMotor2; //Unpaid Intern
    CANPIDController shooterPIDCtrl;

    public RealShooterControl() {

        shooterMotor1 = new CANSparkMax(RobotConstants.SHOOTER_MOTOR_1, MotorType.kBrushless);
        shooterMotor1.restoreFactoryDefaults();
        shooterMotor2 = new CANSparkMax(RobotConstants.SHOOTER_MOTOR_2, MotorType.kBrushless); 
        shooterMotor2.restoreFactoryDefaults();
        shooterMotor1.setIdleMode(IdleMode.kCoast);
        shooterMotor2.setIdleMode(IdleMode.kCoast);
        
        //Increase can bus tx/rx rates
        shooterMotor1.setControlFramePeriodMs(10);
        shooterMotor1.setPeriodicFramePeriod(PeriodicFrame.kStatus0, 3);
        shooterMotor1.setPeriodicFramePeriod(PeriodicFrame.kStatus1, 10);

        shooterMotor1.getEncoder().setVelocityConversionFactor(RobotConstants.SHOOTER_GEAR_RATIO);
        shooterMotor1.getEncoder().setMeasurementPeriod(5000);
        shooterMotor1.getEncoder().setAverageDepth(0);

        //Motors should be inverted to spin outward
        shooterMotor1.setInverted(true);
        shooterMotor2.follow(shooterMotor1, true);

        
        shooterPIDCtrl = shooterMotor1.getPIDController();

        //Spool-up to hold transition conditions
        shooterSpoolUpSteadyStateDbnc = new Calibration("Shooter Steady State Debounce Loops", 17, 0, 500);
        shooterMaxHoldErrorRPM = new Calibration("Shooter Max Hold Error RPM", 20, 0, 5000);

        shooterRPMSetpointFar  = new Calibration("Shooter Far Shot Setpoint RPM", 3200, 0, 5000);
        shooterSendEmVoltage   = new Calibration("Shooter Close Shot Setpoint V", 11.0, 0.0, 14.0);
        shooterShootVoltage   = new Calibration("Shooter Far Shot Shoot Setpoint V", 8.75, 0.0, 14.0);

        holdToShootErrThreshRPM = new Calibration("Shooter Hold To Shoot Error Thresh RPM", 30);
        accelerateToStabilizeThreshRPM = new Calibration("Shooter Accelerate to Stabilize Error Thresh RPM", 150, 0, 5000);
        EjectSpeed = new Calibration("Shooter Eject RPM", 1000, 0, 5000);

        shooterMotorP_spoolup = new Calibration("Shooter Motor SpoolUp P", 0.0007);
        shooterMotorI_spoolup = new Calibration("Shooter Motor SpoolUp I", 0.0000015);
        shooterMotorD_spoolup = new Calibration("Shooter Motor SpoolUp D", 0.017);
        shooterMotorF_spoolup = new Calibration("Shooter Motor SpoolUp F", 0.00020);
        shooterMotorIZone_spoolup = new Calibration("Shooter Motor SpoolUp Izone", 100.0);

        //Shooter error derivative smoothing filter
        shooterSpeedErrorDerivFilter = new AveragingFilter(5, 0);

        //Shooter loaded calculation
        loadedDebounceRPMCal = new Calibration("Shooter Loaded RPM", shooterRPMSetpointFar.get()-100);

        commonInit();

        updateGains(true);

        shooterMotor1.burnFlash();
        shooterMotor2.burnFlash();

        currentStateShooter = ShooterCtrlMode.Stop;
        previousStateShooter = ShooterCtrlMode.Stop;

        adjustedSetpointRPM = shooterRPMSetpointFar.get() + shotAdjustmentRPM;

        // Kick off monitor in brand new thread.
        // Thanks to Team 254 for an example of how to do this!
        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(!Thread.currentThread().isInterrupted()){
                        timeTracker.run(RealShooterControl.getInstance(), RealShooterControl.class.getMethod("update"));
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        //Set up thread properties and start it off
        monitorThread.setName("CasseroleRealShooterControl");
        monitorThread.setPriority(Thread.MAX_PRIORITY - 1);
        monitorThread.start();

    }

    public void updateGains(boolean forceChange) {
        if(forceChange || haveCalsChanged()) {
            
            shooterPIDCtrl.setP(shooterMotorP_spoolup.get(), SPOOLUP_PID_SLOT_ID);
            shooterPIDCtrl.setI(shooterMotorI_spoolup.get(), SPOOLUP_PID_SLOT_ID);
            shooterPIDCtrl.setD(shooterMotorD_spoolup.get(), SPOOLUP_PID_SLOT_ID);
            shooterPIDCtrl.setFF(shooterMotorF_spoolup.get(), SPOOLUP_PID_SLOT_ID);
            shooterPIDCtrl.setIZone(shooterMotorIZone_spoolup.get(), SPOOLUP_PID_SLOT_ID);
            calsUpdated = true;
        }
    }

    private boolean haveCalsChanged() {
        return shooterMotorP_spoolup.isChanged() || 
               shooterMotorI_spoolup.isChanged() || 
               shooterMotorD_spoolup.isChanged() || 
               shooterMotorF_spoolup.isChanged() ||
               shooterMotorIZone_spoolup.isChanged();
    }
    

    public void update() {
        double shooterSetpointRPM = 0;
        double shooterSpeedError = 0;

        //Sample the run command
        ShooterRunCommand runCommandLocal = runCommand;
        
        //Calc desired shooter speed
        adjustedSetpointRPM = shooterRPMSetpointFar.get() + shotAdjustmentRPM;
        
        if (runCommandLocal == ShooterRunCommand.Eject){
            shooterSetpointRPM = EjectSpeed.get();
        }else if (runCommandLocal == ShooterRunCommand.Stop){
            shooterSetpointRPM = 0;
        }else { //For close and far shots, use the closed loop calibration
            shooterSetpointRPM = adjustedSetpointRPM;
        }

        //Get actual speed
        shooterActualSpeed_rpm = shooterMotor1.getEncoder().getVelocity();

        //Switch Control Mode
        if(runCommandLocal == ShooterRunCommand.Stop){
            currentStateShooter = ShooterCtrlMode.Stop;
            shooterAtSteadyStateDebounceCounter = shooterSpoolUpSteadyStateDbnc.get();
            shooterSpeedErrorPrev = 0;
            shooterSpeedErrorAbs = 0;
        } else {
            //When commanded to run....
            shooterSpeedErrorPrev = shooterSpeedErrorAbs;
            shooterSpeedError = shooterActualSpeed_rpm - shooterSetpointRPM;
            shooterSpeedErrorAbs = Math.abs(shooterSpeedError);
            shooterSpeedErrorDeriv = shooterSpeedErrorDerivFilter.filter((shooterSpeedErrorAbs - shooterSpeedErrorPrev)/RobotConstants.MAIN_LOOP_Ts);

            //Handle transition out of stop, and into the "running" modes.
            if(currentStateShooter==ShooterCtrlMode.Stop){
                currentStateShooter=ShooterCtrlMode.Accelerate;
            }

            //Whenever we change the setpoint, go back to accelerate to ensure we get to the right speed
            if(shotAdjustmentChanged){
                currentStateShooter=ShooterCtrlMode.Accelerate;
            }

            //Handle running modes
            if(currentStateShooter==ShooterCtrlMode.Accelerate){
                if(shooterSpeedError > -1.0 * accelerateToStabilizeThreshRPM.get()){
                    currentStateShooter = ShooterCtrlMode.Stabilize;
                }
            } else if(currentStateShooter==ShooterCtrlMode.Stabilize){
                if(shooterSpeedErrorAbs > shooterMaxHoldErrorRPM.get()){
                    currentStateShooter = ShooterCtrlMode.Stabilize; //Stay in Stabilize
                    shooterAtSteadyStateDebounceCounter = shooterSpoolUpSteadyStateDbnc.get();
                } else {
                    if(shooterAtSteadyStateDebounceCounter > 0){
                        //Debounce being below the error threshold
                        shooterAtSteadyStateDebounceCounter--;
                    } else {
                        currentStateShooter = ShooterCtrlMode.HoldForShot; //Go to hold and remain there till shooter is commanded to stop.
                        shooterHoldCmdVoltage = shooterMotor1.getAppliedOutput()*shooterMotor1.getBusVoltage();//Calculate the voltage to hold the shooter. Yes I'm well aware this isn't reallly how the physics works. But hey, we've only got 12 hours till competition, and it seems to help. bla bla bla bla george box all models are wrong something something whateves. 
                    }
                }
            } else if(currentStateShooter == ShooterCtrlMode.HoldForShot) {
                if(shooterSpeedError < -1.0 * holdToShootErrThreshRPM.get()){
                    if(runCommandLocal == ShooterRunCommand.ShotClose){
                        //For close shots, we just blast the motor at full power and send'em
                        currentStateShooter = ShooterCtrlMode.JustGonnaSendEm;
                    } else {
                        //Far shot mode - Only send a single ball through at a time, then closed-loop recover before next ball
                        currentStateShooter = ShooterCtrlMode.Shooting;
                    }
                }
            } else if(currentStateShooter == ShooterCtrlMode.Shooting) {

                if( shooterSpeedErrorDeriv < 0 ){
                    currentStateShooter = ShooterCtrlMode.Accelerate;
                }

            } else if(currentStateShooter == ShooterCtrlMode.JustGonnaSendEm) {
                currentStateShooter = ShooterCtrlMode.JustGonnaSendEm; //Maintain this operational mode until someone commands us off.
            } else {
                currentStateShooter = ShooterCtrlMode.Stop; //ERROR software team forgot to do a thing
            }
        }

        if(currentStateShooter != previousStateShooter){
            shooterPIDCtrl.setIAccum(0);

            if(currentStateShooter == ShooterCtrlMode.Accelerate){
                shooterMotor1.setOpenLoopRampRate(0.8);
                shooterMotor2.setOpenLoopRampRate(0.8);
            } else {
                shooterMotor1.setOpenLoopRampRate(0.0);
                shooterMotor2.setOpenLoopRampRate(0.0);
            }
        }

        // Send commands to the motor
        if(currentStateShooter == ShooterCtrlMode.HoldForShot){
            shooterPIDCtrl.setReference(shooterHoldCmdVoltage, ControlType.kVoltage); //A la 254 in 2017 - hold shooter voltage during first part of the shot
        } else if(currentStateShooter == ShooterCtrlMode.Shooting) {
            shooterPIDCtrl.setReference(shooterShootVoltage.get(), ControlType.kVoltage); //As the ball traverses the shooter arc, boost the voltage to attempt to overcome the "droop"
        } else if(currentStateShooter == ShooterCtrlMode.Accelerate){
            shooterPIDCtrl.setReference( 1.0, ControlType.kDutyCycle); //Maximum control effort for when we want to get fast, fast.
        } else if(currentStateShooter == ShooterCtrlMode.Stabilize){
                shooterPIDCtrl.setReference(shooterSetpointRPM, ControlType.kVelocity, SPOOLUP_PID_SLOT_ID); //PID control for zeroing-in on the target setpoint speed
        } else if(currentStateShooter == ShooterCtrlMode.JustGonnaSendEm){
            shooterPIDCtrl.setReference(shooterSendEmVoltage.get(), ControlType.kVoltage); //Throw accuracy to the wind. Just chuck them all out the window.
        }else{
            shooterPIDCtrl.setReference(0, ControlType.kVoltage); //Stop.
        }


        //Determine if we're under load or not

        if(currentStateShooter != ShooterCtrlMode.Stop){
            if(currentStateShooter ==ShooterCtrlMode.HoldForShot){
                underLoad=false;
            }else if(shooterActualSpeed_rpm < loadedDebounceRPMCal.get()){
                if(!underLoad){
                    shotCount++;
                }
                underLoad=true;
            }
        }else{
            underLoad=true;
        }

        previousStateShooter = currentStateShooter;
        shotAdjustmentChanged = false; //has been handled by this call to update()


    }

    @Override
    public boolean isUnderLoad(){
        if(currentStateShooter!=ShooterCtrlMode.Stop){
            return underLoad;
        }else{
            return false;
        }
    }

    @Override
    public double getSpeedRPM(){
        return shooterActualSpeed_rpm;
    }

    @Override
    public ShooterCtrlMode getShooterCtrlMode(){
        return currentStateShooter;
    }

    @Override
    public int getShotCount(){
        return shotCount;
    }

    @Override
    public double getAdjustedSetpointRPM() {
        return adjustedSetpointRPM;
    }


}
