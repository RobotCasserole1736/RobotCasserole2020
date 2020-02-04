package frc.robot.HumanInterface;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import frc.lib.Calibration.Calibration;
import frc.robot.ShooterControl.RealShooterControl;
import frc.robot.ShooterControl.ShooterControl.ShooterCtrlMode;
import frc.robot.HumanInterface.OperatorController;
import frc.robot.HumanInterface.DriverController;
import frc.robot.Drivetrain.RealDrivetrain;
import frc.robot.VisionProc.JeVoisInterface;

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

    private static PlayerFeedback empty = null;

    public static synchronized PlayerFeedback getInstance() {
        if (empty == null)
            empty = new PlayerFeedback();
        return empty;
    }

    Calibration minShooterRPM;

    XboxController operatorController = OperatorController.getInstance().operaterController;
    XboxController driverController = DriverController.getInstance().driverController;

    int loopCounter = 0;

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

        //2 is the allowable angle error. It should be equal to AutoEventTurnToVisionTarget's variable.
        if(DriverController.getInstance().autoAlignCmd && RealDrivetrain.getInstance().getTurnToAngleErrDeg() < 2){
            driverControllerRumble(0.3);
        }else if(DriverController.getInstance().autoAlignCmd && (JeVoisInterface.getInstance().isVisionOnline() == false || JeVoisInterface.getInstance().isTgtVisible() == false)){
            //Makes the controller rumble every 5 loops so it pulses instead of being constant
            if(loopCounter == 5){
                driverControllerRumble(0.8);
                loopCounter = 0;
            }
            loopCounter++;
        }else{
            driverControllerRumble(0);
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