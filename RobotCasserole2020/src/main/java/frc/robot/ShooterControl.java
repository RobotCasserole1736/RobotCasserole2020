/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;

/**
 * Add your docs here.
 */
public class ShooterControl {
    private static ShooterControl instance = null;

    Calibration shooterRPMSetpoint;

    Calibration shooterMotorF;
    Calibration shooterMotorP;
    Calibration shooterMotorI;
    Calibration shooterMotorD;

    Signal rpmDesiredSig;
    Signal rpmActualSig;

    CANSparkMax shooterMotor1; //Master
    CANSparkMax shooterMotor2; //Unpaid Intern
    CANPIDController shooterPIDCtrl;

    boolean run;

	public static synchronized ShooterControl getInstance() {
		if(instance == null)
		instance = new ShooterControl();
		return instance;
    }

    private ShooterControl() {

        shooterRPMSetpoint = new Calibration("Shooter RPM Setpoint", 0);

        shooterMotorF = new Calibration("Shooter Motor F", 0); 
        shooterMotorP = new Calibration("Shooter Motor P", 0);
        shooterMotorI = new Calibration("Shooter Motor I", 0);
        shooterMotorD = new Calibration("Shooter Motor D", 0);

        rpmDesiredSig = new Signal("Desired Shooter RPM", "RPM");
        rpmActualSig = new Signal("Actual Shooter RPM", "RPM");

        shooterMotor1 = new CANSparkMax(RobotConstants.SHOOTER_MOTOR_1, MotorType.kBrushless);
        shooterMotor2 = new CANSparkMax(RobotConstants.SHOOTER_MOTOR_2, MotorType.kBrushless); 

        shooterMotor2.follow(shooterMotor1);

        shooterPIDCtrl = shooterMotor1.getPIDController();

        shooterPIDCtrl.setFF(shooterMotorF.get());
        shooterPIDCtrl.setP(shooterMotorP.get());
        shooterPIDCtrl.setI(shooterMotorI.get());
        shooterPIDCtrl.setD(shooterMotorD.get());

    }

    public void setRun(final boolean runCmd) {
        run = runCmd;
    }


    public void update() {

        //Sets Motor's RPM
        if(run == true) {
            shooterPIDCtrl.setReference(shooterRPMSetpoint.get(), ControlType.kVelocity); 
        }else if(run == false) {
            shooterPIDCtrl.setReference(0, ControlType.kVelocity);
        }
    }
}
