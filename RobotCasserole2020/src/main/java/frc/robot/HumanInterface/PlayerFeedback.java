package frc.robot.HumanInterface;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.XboxController;
import frc.lib.Calibration.Calibration;
import frc.robot.ShooterControl.RealShooterControl;
import frc.robot.ShooterControl.ShooterControl.ShooterCtrlMode;
import frc.robot.HumanInterface.OperatorController;
import frc.robot.HumanInterface.DriverController;
import frc.robot.Drivetrain.RealDrivetrain;
import frc.robot.VisionProc.CasseroleVision;

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

    private static PlayerFeedback inst = null;

    public static synchronized PlayerFeedback getInstance() {
        if (inst == null)
            inst = new PlayerFeedback();
        return inst;
    }

    XboxController operatorController = OperatorController.getInstance().operaterController;
    XboxController driverController = DriverController.getInstance().driverController;

    int loopCounter = 0;

    // This is the private constructor that will be called once by getInstance() and
    // it should instantiate anything that will be required by the class
    private PlayerFeedback() {

    }

    public void update(){
        ShooterCtrlMode currentShooterCtrlMode = RealShooterControl.getInstance().getShooterCtrlMode();
        
        //Makes the operator controller rumble when the robot is preparing to shoot and for each ball that is shot
        if(!DriverStation.getInstance().isAutonomous()){
            if(currentShooterCtrlMode == ShooterCtrlMode.Stabilize || currentShooterCtrlMode == ShooterCtrlMode.Accelerate){
                OperatorController.getInstance().rumble(0.4);
            }else{
                OperatorController.getInstance().rumble(0);
            }
        }else{
            OperatorController.getInstance().rumble(0);
        }
        

        //2 is the allowable angle error. It should be equal to AutoEventTurnToVisionTarget's variable.
        if(!DriverStation.getInstance().isAutonomous()){
            if(DriverController.getInstance().autoAlignCmd && RealDrivetrain.getInstance().getTurnToAngleErrDeg() < 0.5){
                DriverController.getInstance().rumble(0.3);
            }else if(DriverController.getInstance().autoAlignCmd && (CasseroleVision.getInstance().isVisionOnline() == false || CasseroleVision.getInstance().isTgtVisible() == false)){
                //Makes the controller rumble every 5 loops so it pulses instead of being constant
                if(loopCounter == 10){
                    loopCounter = 0;
                } else {
                    loopCounter++;
                }
    
                if(loopCounter < 5){
                    DriverController.getInstance().rumble(0.8);
                } else {
                    DriverController.getInstance().rumble(0);
                }
    
            }else{
                DriverController.getInstance().rumble(0);
            }
        }else{
            DriverController.getInstance().rumble(0);
        }

    }
	
}   