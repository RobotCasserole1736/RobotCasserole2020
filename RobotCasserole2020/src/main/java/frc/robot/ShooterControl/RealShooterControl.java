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

    /* Allowable shooter operation modes*/
    public enum ShooterCtrlMode {
        SpoolUp(0),   
        HoldSpeed(1),  
        Stop(2);

        public final int value;
        private ShooterCtrlMode(int value) {
            this.value = value;
        }
    }
    
    boolean underLoad = false;
    int currentStateShooter;
    double timer = 0;
    double outputTime;

    Calibration shooterRPMSetpoint;

    Calibration shooterMotorP;
    Calibration shooterMotorI;
    Calibration shooterMotorD;
    Calibration shooterMotorF;

    Calibration loadedThresholdShooter;
    Calibration unloadedThresholdShooter;
    Calibration loadedTimer;
    Calibration unloadedTimer;

    Signal rpmDesiredSig;
    Signal rpmActualSig;

    Signal isUnderLoad;

    CANSparkMax shooterMotor1; //Master
    CANSparkMax shooterMotor2; //Unpaid Intern
    CANPIDController shooterPIDCtrl;

    public RealShooterControl() {

        shooterRPMSetpoint = new Calibration("Shooter RPM Setpoint", 0);

        shooterMotorP = new Calibration("Shooter Motor P", 0);
        shooterMotorI = new Calibration("Shooter Motor I", 0);
        shooterMotorD = new Calibration("Shooter Motor D", 0);
        shooterMotorF = new Calibration("Shooter Motor F", 0);

        loadedThresholdShooter = new Calibration("loadedThresholdShooter", 20);
        unloadedThresholdShooter = new Calibration("unloadedThresholdShooter", 10);
        loadedTimer = new Calibration("loaded Timer", 2);
        unloadedTimer = new Calibration("unloaded Timer ", 2);

        rpmDesiredSig = new Signal("Desired Shooter RPM", "RPM");
        rpmActualSig = new Signal("Actual Shooter RPM", "RPM");

        isUnderLoad = new Signal("Shooter Under Load","boolean");

        shooterMotor1 = new CANSparkMax(RobotConstants.SHOOTER_MOTOR_1, MotorType.kBrushless);
        shooterMotor2 = new CANSparkMax(RobotConstants.SHOOTER_MOTOR_2, MotorType.kBrushless); 

        shooterMotor2.follow(shooterMotor1);

        shooterPIDCtrl = shooterMotor1.getPIDController();

        shooterPIDCtrl.setP(shooterMotorP.get());
        shooterPIDCtrl.setI(shooterMotorI.get());
        shooterPIDCtrl.setD(shooterMotorD.get());
        shooterPIDCtrl.setFF(shooterMotorF.get());

    }

    public void update() {

        //Sets Motor's RPM
        if(run == true) {
            shooterPIDCtrl.setReference(shooterRPMSetpoint.get(), ControlType.kVelocity); 
        }else if(run == false) {
            shooterPIDCtrl.setReference(0, ControlType.kVelocity);
        }

        //2 means the hold state
        if(currentStateShooter == 2){
            if(shooterMotor1.getOutputCurrent() > loadedThresholdShooter.get()){
                if (timer==0){
                    timer=Timer.getFPGATimestamp();
                }
                outputTime = Timer.getFPGATimestamp()-timer;
                if(outputTime > loadedTimer.get()){
                    underLoad = true;
                    timer = 0;
                }
            }else if((shooterMotor1.getOutputCurrent() < unloadedThresholdShooter.get())){
                if (timer==0){
                    timer=Timer.getFPGATimestamp();
                }
                outputTime = Timer.getFPGATimestamp()-timer;            
                if(outputTime > unloadedTimer.get()){
                    underLoad = false;
                    timer = 0;
                }
            }
        }else{
            underLoad = false;
        }
    }

    public boolean isUnderLoad(){
        return this.underLoad;
    }
}
