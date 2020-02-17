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
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;

/**
 * Add your docs here.
 */
public class RealShooterControl extends ShooterControl {

    private final int SPOOLUP_PID_SLOT_ID = 0;
    private final int HOLD_PID_SLOT_ID = 1;
    
    boolean underLoad = false;
    ShooterCtrlMode currentStateShooter;
    ShooterCtrlMode previousStateShooter;
    double shooterActualSpeed_rpm; //Arbitrated shooter wheel speed
    double shooterMotor1Speed_rpm; //Motor 1 measured speed
    double shooterMotor2Speed_rpm; //Motor 2 measured speed - Ideally this should be same as 1, but maybe not if faulted
    double shooterAtSteadyStateDebounceCounter;
    double shooterReadyDebounceCounter;
    double shooterHoldCmdDCPct = 0;
    int shotCount=0;

    Calibration shooterMaxHoldErrorRPM;
    Calibration shooterMaxErrorRPM;
    Calibration shooterSpoolUpSteadyStateDbnc;
    Calibration shooterReadyStateDbnc;
    Calibration shooterHoldKickOutRPM;
    Calibration EjectSpeed;

    Calibration shooterMotorP_spoolup;
    Calibration shooterMotorI_spoolup;
    Calibration shooterMotorD_spoolup;
    Calibration shooterMotorF_spoolup;
    Calibration shooterMotorIZone_spoolup;
    Calibration percentSpeed;

    Calibration loadedDebounceRPMCal;

    boolean calsUpdated;

    Signal motor1SpeedSig;
    Signal motor2SpeedSig;
    Signal isUnderLoadSig;
    Signal shotCountSig;
    Signal shooterMotor1CurrentSig;
    Signal shooterMotor2CurrentSig;
    Signal shooterMotor1Percent;
    Signal shooterMotor2Percent;
    Signal shooterMotor1InVoltage;
    Signal shooterMotor2InVoltage;

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

        shooterMotor1.setSmartCurrentLimit(60);
        shooterMotor2.setSmartCurrentLimit(60);

        //Motors should be inverted to spin outward
        shooterMotor1.setInverted(true);
        //Additionally, 2 is mirrored in position from 1, so we'll want to invert it again.
        shooterMotor2.follow(shooterMotor1, true);

        shooterMotor1.setClosedLoopRampRate(0.25);
        
        
        shooterPIDCtrl = shooterMotor1.getPIDController();

        shooterSpoolUpSteadyStateDbnc = new Calibration("Shooter Steady State Debounce Loops", 15);
        shooterReadyStateDbnc = new Calibration("Shooter Ready to Shoot Debounce Loops", 5);
        shooterRPMSetpointFar  = new Calibration("Shooter Far Shot Setpoint RPM", 3500);
        shooterRPMSetpointClose= new Calibration("Shooter Close Shot Setpoint RPM", 3500);
        shooterMaxHoldErrorRPM = new Calibration("Shooter Max Hold Error RPM", 50);
        shooterHoldKickOutRPM = new Calibration("Shooter Hold Error Kickout Thresh RPM", 400);
        EjectSpeed = new Calibration("Shooter Eject RPM", 1000);

        shooterMotorP_spoolup = new Calibration("Shooter Motor SpoolUp P", 0.0006);
        shooterMotorI_spoolup = new Calibration("Shooter Motor SpoolUp I", 0.00003);
        shooterMotorD_spoolup = new Calibration("Shooter Motor SpoolUp D", 0.000);
        shooterMotorF_spoolup = new Calibration("Shooter Motor SpoolUp F", 0.00020);
        shooterMotorIZone_spoolup = new Calibration("Shooter Motor SpoolUp Izone", 250.0);


        //Shooter loaded calculation
        loadedDebounceRPMCal = new Calibration("Shooter Loaded RPM", shooterRPMSetpointFar.get()-100);

        //Data Logging
        motor1SpeedSig = new Signal("Shooter Motor 1 Speed", "RPM");
        motor2SpeedSig = new Signal("Shooter Motor 2 Speed", "RPM");
        shooterMotor1Percent = new Signal("Shooter Motor 1 Percent", "pct");
        shooterMotor2Percent = new Signal("Shooter Motor 2 Percent", "pct");
        isUnderLoadSig = new Signal("Shooter Under Load","bool");
        shotCountSig = new Signal("Shots Taken","balls");
        shooterMotor1CurrentSig = new Signal("Shooter Motor 1 Current","A");
        shooterMotor2CurrentSig = new Signal("Shooter Motor 2 Current","A");
        shooterMotor1InVoltage = new Signal("Shooter Motor 1 in Voltage", "V");
        shooterMotor2InVoltage = new Signal("Shooter Motor 2 in Voltage", "V");;

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
        
        if(run == ShooterRunCommand.ShotClose) {
            shooterSetpointRPM = shooterRPMSetpointClose.get();
        } else if (run == ShooterRunCommand.ShotFar) {
            shooterSetpointRPM = shooterRPMSetpointFar.get();
        } else if (run == ShooterRunCommand.Eject){
            shooterSetpointRPM = EjectSpeed.get();
        }else if (run == ShooterRunCommand.Stop){
            shooterSetpointRPM = 0;
        }

        //Calcualte actual speed from both motors for redundancy
        shooterMotor1Speed_rpm = shooterMotor1.getEncoder().getVelocity();
        shooterMotor2Speed_rpm = shooterMotor2.getEncoder().getVelocity();
        shooterActualSpeed_rpm = Math.max(shooterMotor1Speed_rpm, shooterMotor2Speed_rpm); //Arbitrarte actual speed as max of both motors

        //Switch Control Mode
        if(run == ShooterRunCommand.Stop){
            currentStateShooter = ShooterCtrlMode.Stop;
            shooterAtSteadyStateDebounceCounter = shooterSpoolUpSteadyStateDbnc.get();
            shooterReadyDebounceCounter = shooterReadyStateDbnc.get();
        } else {
            //When commanded to run....
            double err = Math.abs(shooterActualSpeed_rpm - shooterSetpointRPM);
            if(currentStateShooter==ShooterCtrlMode.Stop){
                currentStateShooter=ShooterCtrlMode.SpoolUp;
            }

            if(currentStateShooter==ShooterCtrlMode.SpoolUp){
                if(err > shooterMaxHoldErrorRPM.get()){
                    currentStateShooter = ShooterCtrlMode.SpoolUp; //Stay in spoolup
                    shooterAtSteadyStateDebounceCounter = shooterSpoolUpSteadyStateDbnc.get();
                } else {
                    if(shooterAtSteadyStateDebounceCounter > 0){
                        //Debounce being below the error threshold
                        shooterAtSteadyStateDebounceCounter--;
                    } else {
                        currentStateShooter = ShooterCtrlMode.HoldSpeed; //Go to hold and remain there till shooter is commanded to stop.
                        shooterHoldCmdDCPct = shooterMotor1.getAppliedOutput();
                    }
                }
            } else {
                if(err > shooterHoldKickOutRPM.get()){
                    currentStateShooter = ShooterCtrlMode.SpoolUp;
                }
            }
        }

        if(currentStateShooter != previousStateShooter){
            if(currentStateShooter == ShooterCtrlMode.HoldSpeed){
                shooterMotor1.setClosedLoopRampRate(0.0);
            } else {
                shooterMotor1.setClosedLoopRampRate(0.25);
            }
            shooterPIDCtrl.setIAccum(0);
        }

        // Send commands to the motor
        if(currentStateShooter == ShooterCtrlMode.HoldSpeed){
            shooterPIDCtrl.setReference(shooterHoldCmdDCPct, ControlType.kDutyCycle); //A la 254 in 2017
        } else if(currentStateShooter == ShooterCtrlMode.SpoolUp){
            shooterPIDCtrl.setReference(shooterSetpointRPM, ControlType.kVelocity, SPOOLUP_PID_SLOT_ID);
        }else{
            shooterPIDCtrl.setReference(0, ControlType.kVoltage);
        }


        //Determine if we're under load or not

        if(currentStateShooter != ShooterCtrlMode.Stop){
            if(currentStateShooter ==ShooterCtrlMode.HoldSpeed){
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
        motor1SpeedSig.addSample(sampleTimeMS, shooterMotor1Speed_rpm);
        motor2SpeedSig.addSample(sampleTimeMS, shooterMotor2Speed_rpm);
        isUnderLoadSig.addSample(sampleTimeMS, underLoad);
        shotCountSig.addSample(sampleTimeMS, shotCount);
        shooterStateCommandSig.addSample(sampleTimeMS, run.value);
        shooterControlModeSig.addSample(sampleTimeMS, currentStateShooter.value);
        shooterMotor1CurrentSig.addSample(sampleTimeMS, shooterMotor1.getOutputCurrent());
        shooterMotor2CurrentSig.addSample(sampleTimeMS, shooterMotor2.getOutputCurrent());
        shooterMotor1Percent.addSample(sampleTimeMS, shooterMotor1.getAppliedOutput());
        shooterMotor2Percent.addSample(sampleTimeMS, shooterMotor2.getAppliedOutput());
        shooterMotor1InVoltage.addSample(sampleTimeMS, shooterMotor1.getBusVoltage());
        shooterMotor2InVoltage.addSample(sampleTimeMS, shooterMotor2.getBusVoltage());
        

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
