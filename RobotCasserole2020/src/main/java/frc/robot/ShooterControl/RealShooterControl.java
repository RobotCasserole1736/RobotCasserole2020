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

import edu.wpi.first.wpilibj.Timer;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;

/**
 * Add your docs here.
 */
public class RealShooterControl extends ShooterControl {

    private final int SPOOLUP_PID_SLOT_ID = 0;
    private final int HOLD_PID_SLOT_ID = 0;
    
    boolean underLoad = false;
    ShooterCtrlMode currentStateShooter;
    ShooterCtrlMode previousStateShooter;
    double timer = 0;
    double outputTime;
    double shooterActualSpeed_rpm; //Arbitrated shooter wheel speed
    double shooterMotor1Speed_rpm; //Motor 1 measured speed
    double shooterMotor2Speed_rpm; //Motor 2 measured speed - Ideally this should be same as 1, but maybe not if faulted

    Calibration shooterMaxHoldErrorRPM;

    Calibration shooterMotorP_spoolup;
    Calibration shooterMotorI_spoolup;
    Calibration shooterMotorD_spoolup;
    Calibration shooterMotorF_spoolup;

    Calibration shooterMotorP_hold;
    Calibration shooterMotorI_hold;
    Calibration shooterMotorD_hold;
    Calibration shooterMotorF_hold;

    Calibration loadedThresholdShooter;
    Calibration unloadedThresholdShooter;
    Calibration loadedDebounceCal;
    Calibration unloadedDebounceCal;

    boolean calsUpdated;

    Signal motor1SpeedSig;
    Signal motor2SpeedSig;
    Signal isUnderLoadSig;
    Signal shooterMotor1CurrentSig;
    Signal shooterMotor2CurrentSig;

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

        shooterRPMSetpointFar  = new Calibration("Shooter Far Shot Setpoint RPM", 4500);
        shooterRPMSetpointClose= new Calibration("Shooter Close Shot Setpoint RPM", 5000);
        shooterMaxHoldErrorRPM = new Calibration("Shooter Max Hold Error RPM", 500);

        shooterMotorP_spoolup = new Calibration("Shooter Motor SpoolUp P", 0.00004);
        shooterMotorI_spoolup = new Calibration("Shooter Motor SpoolUp I", 0);
        shooterMotorD_spoolup = new Calibration("Shooter Motor SpoolUp D", 0);
        shooterMotorF_spoolup = new Calibration("Shooter Motor SpoolUp F", 0.0008);
        shooterMotorP_hold    = new Calibration("Shooter Motor hold P", 0.00004);
        shooterMotorI_hold    = new Calibration("Shooter Motor hold I", 0);
        shooterMotorD_hold    = new Calibration("Shooter Motor hold D", 0);
        shooterMotorF_hold    = new Calibration("Shooter Motor hold F", 0.0008);

        //Shooter loaded calculation
        loadedThresholdShooter = new Calibration("Shooter Loaded Threshold A", 20);
        unloadedThresholdShooter = new Calibration("Shooter Unloaded Threshold A", 10);
        loadedDebounceCal = new Calibration("Shooter Loaded Timer S", 0.25);
        unloadedDebounceCal = new Calibration("Shooter Unloaded Timer S", 2);

        //Data Logging
        motor1SpeedSig = new Signal("Shooter Motor 1 Speed", "RPM");
        motor2SpeedSig = new Signal("Shooter Motor 2 Speed", "RPM");
        isUnderLoadSig = new Signal("Shooter Under Load","bool");
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
            shooterPIDCtrl.setP(shooterMotorP_hold.get(), HOLD_PID_SLOT_ID);
            shooterPIDCtrl.setI(shooterMotorI_hold.get(), HOLD_PID_SLOT_ID);
            shooterPIDCtrl.setD(shooterMotorD_hold.get(), HOLD_PID_SLOT_ID);
            shooterPIDCtrl.setFF(shooterMotorF_hold.get(), HOLD_PID_SLOT_ID);
            calsUpdated = true;
        }
    }

    private boolean haveCalsChanged() {
        return shooterMotorP_spoolup.isChanged() || 
               shooterMotorI_spoolup.isChanged() || 
               shooterMotorD_spoolup.isChanged() || 
               shooterMotorF_spoolup.isChanged() || 
               shooterMotorP_hold.isChanged() || 
               shooterMotorI_hold.isChanged() || 
               shooterMotorD_hold.isChanged() || 
               shooterMotorF_hold.isChanged();
    }
    

    public void update() {

        //Calc desired shooter speed
        double shooterSetpointRPM = 0;
        if(run == ShooterRunCommand.ShotClose) {
            shooterSetpointRPM = shooterRPMSetpointClose.get();
        } else if (run == ShooterRunCommand.ShotFar) {
            shooterSetpointRPM = shooterRPMSetpointFar.get();
        } else if (run == ShooterRunCommand.Stop){
            shooterSetpointRPM = 0;
        }

        //Calcualte actual speed from both motors for redundancy
        shooterMotor1Speed_rpm = shooterMotor1.getEncoder().getVelocity();
        shooterMotor2Speed_rpm = shooterMotor2.getEncoder().getVelocity();
        shooterActualSpeed_rpm = Math.max(shooterMotor1Speed_rpm, shooterMotor2Speed_rpm); //Arbitrarte actual speed as max of both motors

        //Switch Control Mode
        if(run == ShooterRunCommand.Stop){
            currentStateShooter = ShooterCtrlMode.Stop;
        } else {
            double err = Math.abs(shooterActualSpeed_rpm - shooterSetpointRPM);
            if(err < shooterMaxHoldErrorRPM.get()){
                currentStateShooter = ShooterCtrlMode.HoldSpeed;
            } else {
                currentStateShooter = ShooterCtrlMode.SpoolUp;
            }
        }

        if(currentStateShooter != previousStateShooter){
            if(currentStateShooter == ShooterCtrlMode.HoldSpeed){
                shooterMotor1.setClosedLoopRampRate(0.0);
            } else {
                shooterMotor1.setClosedLoopRampRate(0.25);
            }
        }

        // Send commands to the motor
        if(currentStateShooter == ShooterCtrlMode.HoldSpeed){
            shooterPIDCtrl.setReference(shooterSetpointRPM, ControlType.kVelocity, HOLD_PID_SLOT_ID);
        } else if(currentStateShooter == ShooterCtrlMode.SpoolUp){
            shooterPIDCtrl.setReference(shooterSetpointRPM, ControlType.kVelocity, SPOOLUP_PID_SLOT_ID);
        } else {
            shooterPIDCtrl.setReference(0, ControlType.kVoltage);
        }


        //Determine if we're under load or not
        double motor1Current = shooterMotor1.getOutputCurrent();
        double motor2Current = shooterMotor2.getOutputCurrent();
        if(currentStateShooter == ShooterCtrlMode.HoldSpeed){
            if(motor1Current > loadedThresholdShooter.get()){
                if (timer==0){
                    timer=Timer.getFPGATimestamp();
                }
                outputTime = Timer.getFPGATimestamp()-timer;
                if(outputTime > loadedDebounceCal.get()){
                    underLoad = true;
                    timer = 0;
                }
            }else if((motor1Current< unloadedThresholdShooter.get())){
                if (timer==0){
                    timer=Timer.getFPGATimestamp();
                }
                outputTime = Timer.getFPGATimestamp()-timer;            
                if(outputTime > unloadedDebounceCal.get()){
                    underLoad = false;
                    timer = 0;
                }
            }
        }else{
            underLoad = false;
        }

        previousStateShooter = currentStateShooter;

        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        rpmDesiredSig.addSample(sampleTimeMS, shooterSetpointRPM);
        rpmActualSig.addSample(sampleTimeMS, shooterActualSpeed_rpm);
        motor1SpeedSig.addSample(sampleTimeMS, shooterMotor1Speed_rpm);
        motor2SpeedSig.addSample(sampleTimeMS, shooterMotor2Speed_rpm);
        isUnderLoadSig.addSample(sampleTimeMS, underLoad);
        shooterStateCommandSig.addSample(sampleTimeMS, run.value);
        shooterControlModeSig.addSample(sampleTimeMS, currentStateShooter.value);
        shooterMotor1CurrentSig.addSample(sampleTimeMS, motor1Current);
        shooterMotor2CurrentSig.addSample(sampleTimeMS, motor2Current);
        

    }

    @Override
    public boolean isUnderLoad(){
        return this.underLoad;
    }

    @Override
    public double getSpeedRPM(){
        return shooterActualSpeed_rpm;
    }

    public ShooterCtrlMode getShooterCtrlMode(){
        return currentStateShooter;
    }
  
}
