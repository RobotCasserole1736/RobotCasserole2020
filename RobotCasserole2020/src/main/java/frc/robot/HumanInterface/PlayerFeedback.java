package frc.robot.HumanInterface;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import frc.lib.Calibration.Calibration;
import frc.robot.ShooterControl.RealShooterControl;
import frc.robot.ShooterControl.ShooterControl.ShooterCtrlMode;
import frc.robot.HumanInterface.OperatorController;

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

public class PlayerFeedback {

    Calibration minShooterRPM;

    XboxController operatorController = OperatorController.getInstance().operaterController;
    XboxController driverController = DriverController.getInstance().driverController;

    private Joystick joystick;

    private static PlayerFeedback empty = null;

    public static synchronized PlayerFeedback getInstance() {
        if (empty == null)
            empty = new PlayerFeedback();
        return empty;
    }

    // This is the private constructor that will be called once by getInstance() and
    // it should instantiate anything that will be required by the class
    public PlayerFeedback() {
        minShooterRPM = new Calibration("Minimum Shooter RPM for Rumble", 6000);
    }

    public void update(){
        ShooterCtrlMode currentShooterCtrlMode = RealShooterControl.getInstance().getShooterCtrlMode();
        double actualShooterRPM = RealShooterControl.getInstance().getSpeedRPM();
        
        //Makes the operator controller rumble when the robot is preparing to shoot and for each ball that is shot
        if((currentShooterCtrlMode == ShooterCtrlMode.SpoolUp || currentShooterCtrlMode == ShooterCtrlMode.HoldSpeed) && actualShooterRPM < minShooterRPM.get()){
            operatorControllerRumble(0.4);
        }else{
            operatorControllerRumble(0);
        }

    }

    void operatorControllerRumble(double strength) {
        operatorController.setRumble(RumbleType.kLeftRumble, strength);
        operatorController.setRumble(RumbleType.kRightRumble, strength);
    }

    void driverControllerRumble(double strength) {
        driverController.setRumble(RumbleType.kLeftRumble, strength);
        driverController.setRumble(RumbleType.kRightRumble, strength);
    }
	
}