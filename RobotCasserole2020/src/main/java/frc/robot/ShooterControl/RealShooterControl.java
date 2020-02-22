/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2020 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.ShooterControl;

import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.lib.SignalMath.AveragingFilter;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;

/**
 * Add your docs here.
 */
public class RealShooterControl extends ShooterControl {

    private final int SPOOLUP_PID_SLOT_ID = 0;
    
    boolean underLoad = false;
    ShooterCtrlMode currentStateShooter;
    ShooterCtrlMode previousStateShooter;
    double shooterActualSpeed_rpm; //Arbitrated shooter wheel speed
    double shooterAtSteadyStateDebounceCounter;
    double shooterHoldCmdDCPct = 0;

    double shooterSpeedError = 0;
    double shooterSpeedErrorPrev = 0;
    double shooterSpeedErrorDeriv = 0;
    AveragingFilter shooterSpeedErrorDerivFilter;

    int shotCount=0;

    Calibration shooterMaxHoldErrorRPM;
    Calibration shooterMaxErrorRPM;
    Calibration shooterSpoolUpSteadyStateDbnc;
    Calibration shooterReadyStateDbnc;
    Calibration holdToShootErrThreshRPM;
    Calibration shootToSpoolupThreshRPM;
    Calibration EjectSpeed;

    Calibration shooterMotorP_spoolup;
    Calibration shooterMotorI_spoolup;
    Calibration shooterMotorD_spoolup;
    Calibration shooterMotorF_spoolup;
    Calibration shooterMotorIZone_spoolup;
    Calibration percentSpeed;

    Calibration loadedDebounceRPMCal;

    boolean calsUpdated;

    Signal isUnderLoadSig;
    Signal shotCountSig;
    Signal shooterMotor1CurrentSig;
    Signal shooterMotor2CurrentSig;
    Signal shooterMotor1Percent;

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
        

        shooterMotor1.getEncoder().setVelocityConversionFactor(RobotConstants.SHOOTER_GEAR_RATIO);

        shooterMotor1.setSmartCurrentLimit(90);
        shooterMotor2.setSmartCurrentLimit(90);

        //Motors should be inverted to spin outward
        shooterMotor1.setInverted(true);
        //Additionally, 2 is mirrored in position from 1, so we'll want to invert it again.
        shooterMotor2.follow(shooterMotor1, true);

        shooterMotor1.setClosedLoopRampRate(0.25);
        
        
        shooterPIDCtrl = shooterMotor1.getPIDController();

        //Spool-up to hold transition conditions
        shooterSpoolUpSteadyStateDbnc = new Calibration("Shooter Steady State Debounce Loops", 25);
        shooterMaxHoldErrorRPM = new Calibration("Shooter Max Hold Error RPM", 30);

        shooterRPMSetpointFar  = new Calibration("Shooter Far Shot Setpoint RPM", 3650);
        shooterSendEmVoltage   = new Calibration("Shooter Far Shot Setpoint V", 12.0, 0.0, 14.0);

        holdToShootErrThreshRPM = new Calibration("Shooter Hold To Shoot Error Thresh RPM", 75);
        shootToSpoolupThreshRPM = new Calibration("Shooter Shoot To Spoolup Error Thresh RPM", 400);
        EjectSpeed = new Calibration("Shooter Eject RPM", 1000);

        shooterMotorP_spoolup = new Calibration("Shooter Motor SpoolUp P", 0.0006);
        shooterMotorI_spoolup = new Calibration("Shooter Motor SpoolUp I", 0.0000015);
        shooterMotorD_spoolup = new Calibration("Shooter Motor SpoolUp D", 0.003);
        shooterMotorF_spoolup = new Calibration("Shooter Motor SpoolUp F", 0.00020);
        shooterMotorIZone_spoolup = new Calibration("Shooter Motor SpoolUp Izone", 100.0);

        //Shooter error derivative smoothing filter
        shooterSpeedErrorDerivFilter = new AveragingFilter(5, 0);

        //Shooter loaded calculation
        loadedDebounceRPMCal = new Calibration("Shooter Loaded RPM", shooterRPMSetpointFar.get()-100);

        //Data Logging
        shooterMotor1Percent = new Signal("Shooter Motor 1 Percent", "pct");
        isUnderLoadSig = new Signal("Shooter Under Load","bool");
        shotCountSig = new Signal("Shots Taken","balls");
        shooterMotor1CurrentSig = new Signal("Shooter Motor 1 Current","A");
        shooterMotor2CurrentSig = new Signal("Shooter Motor 2 Current","A");

        commonInit();

        updateGains(true);

        shooterMotor1.burnFlash();
        shooterMotor2.burnFlash();

        currentStateShooter = ShooterCtrlMode.Stop;
        previousStateShooter = ShooterCtrlMode.Stop;

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
        
        //Calc desired shooter speed
        
        if (runCommand == ShooterRunCommand.Eject){
            shooterSetpointRPM = EjectSpeed.get();
        }else if (runCommand == ShooterRunCommand.Stop){
            shooterSetpointRPM = 0;
        }else { //For close and far shots, use the closed loop calibration
            shooterSetpointRPM = shooterRPMSetpointFar.get();
        }

        //Get actual speed
        shooterActualSpeed_rpm = shooterMotor1.getEncoder().getVelocity();

        //Switch Control Mode
        if(runCommand == ShooterRunCommand.Stop){
            currentStateShooter = ShooterCtrlMode.Stop;
            shooterAtSteadyStateDebounceCounter = shooterSpoolUpSteadyStateDbnc.get();
            shooterSpeedErrorPrev = 0;
            shooterSpeedError = 0;
        } else {
            //When commanded to run....
            shooterSpeedErrorPrev = shooterSpeedError;
            shooterSpeedError = Math.abs(shooterActualSpeed_rpm - shooterSetpointRPM);
            shooterSpeedErrorDeriv = shooterSpeedErrorDerivFilter.filter((shooterSpeedError - shooterSpeedErrorPrev)/RobotConstants.MAIN_LOOP_Ts);

            //Handle transition out of stop, and into the "running" modes.
            if(currentStateShooter==ShooterCtrlMode.Stop){
                currentStateShooter=ShooterCtrlMode.SpoolUp;
            }

            //Handle running modes
            if(currentStateShooter==ShooterCtrlMode.SpoolUp){
                if(shooterSpeedError > shooterMaxHoldErrorRPM.get()){
                    currentStateShooter = ShooterCtrlMode.SpoolUp; //Stay in spoolup
                    shooterAtSteadyStateDebounceCounter = shooterSpoolUpSteadyStateDbnc.get();
                } else {
                    if(shooterAtSteadyStateDebounceCounter > 0){
                        //Debounce being below the error threshold
                        shooterAtSteadyStateDebounceCounter--;
                    } else {
                        currentStateShooter = ShooterCtrlMode.HoldForShot; //Go to hold and remain there till shooter is commanded to stop.
                        shooterHoldCmdDCPct = shooterMotor1.getAppliedOutput();
                    }
                }
            } else if(currentStateShooter == ShooterCtrlMode.HoldForShot) {
                if(shooterSpeedError > holdToShootErrThreshRPM.get()){
                    if(runCommand == ShooterRunCommand.ShotClose){
                        //For close shots, we just blast the motor at full power and send'em
                        currentStateShooter = ShooterCtrlMode.JustGonnaSendEm;
                    } else {
                        //Far shot mode - Only send a single ball through at a time, then closed-loop recover before next ball
                        currentStateShooter = ShooterCtrlMode.Shooting;
                    }
                }
            } else if(currentStateShooter == ShooterCtrlMode.Shooting) {

                if(shooterSpeedError < shootToSpoolupThreshRPM.get() && shooterSpeedErrorDeriv < 0 ){
                    currentStateShooter = ShooterCtrlMode.SpoolUp;
                }

            } else if(currentStateShooter == ShooterCtrlMode.JustGonnaSendEm) {
                currentStateShooter = ShooterCtrlMode.JustGonnaSendEm; //Maintain this operational mode until someone commands us off.
            } else {
                currentStateShooter = ShooterCtrlMode.Stop; //ERROR software team forgot to do a thing
            }
        }

        if(currentStateShooter != previousStateShooter){
            if(currentStateShooter != ShooterCtrlMode.SpoolUp){
                shooterMotor1.setClosedLoopRampRate(0.0);
            } else {
                shooterMotor1.setClosedLoopRampRate(0.25);
            }
            shooterPIDCtrl.setIAccum(0);
        }

        // Send commands to the motor
        if(currentStateShooter == ShooterCtrlMode.HoldForShot || currentStateShooter == ShooterCtrlMode.Shooting){
            shooterPIDCtrl.setReference(shooterHoldCmdDCPct, ControlType.kDutyCycle); //A la 254 in 2017
        } else if(currentStateShooter == ShooterCtrlMode.SpoolUp){
            shooterPIDCtrl.setReference(shooterSetpointRPM, ControlType.kVelocity, SPOOLUP_PID_SLOT_ID);
        } else if(currentStateShooter == ShooterCtrlMode.JustGonnaSendEm){
            shooterPIDCtrl.setReference(shooterSendEmVoltage.get(), ControlType.kVoltage); 
        }else{
            shooterPIDCtrl.setReference(0, ControlType.kVoltage);
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


        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        rpmDesiredSig.addSample(sampleTimeMS, shooterSetpointRPM);
        rpmActualSig.addSample(sampleTimeMS, shooterActualSpeed_rpm);
        isUnderLoadSig.addSample(sampleTimeMS, underLoad);
        shotCountSig.addSample(sampleTimeMS, shotCount);
        shooterStateCommandSig.addSample(sampleTimeMS, runCommand.value);
        shooterControlModeSig.addSample(sampleTimeMS, currentStateShooter.value);
        shooterMotor1CurrentSig.addSample(sampleTimeMS, shooterMotor1.getOutputCurrent());
        shooterMotor2CurrentSig.addSample(sampleTimeMS, shooterMotor2.getOutputCurrent());
        shooterMotor1Percent.addSample(sampleTimeMS, shooterMotor1.getAppliedOutput());

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


}
