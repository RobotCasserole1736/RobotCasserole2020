package frc.robot.HumanInterface;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import frc.robot.Drivetrain.Utils;
import edu.wpi.first.wpilibj.GenericHID;

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

public class DriverController {
    XboxController driverController;
    private static DriverController instance = null;
    boolean compressorEnableReq = true;
    boolean compressorDisableReq = false;

    
    public static synchronized DriverController getInstance() {
		if(instance == null)
		instance = new DriverController();
		return instance;
	}

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private DriverController() {
        //TODO - Open a particular xBOX controller
        driverController = new XboxController(0);

        //TODO - move these to approprate getter functions
        //driverFwdRevCmd = Utils.ctrlAxisScale(-1*frCmd,  joystickExpScaleFactor.get(), joystickDeadzone.get());
        //driverRotateCmd = Utils.ctrlAxisScale(   rCmd, joystickExpScaleFactor.get(), joystickDeadzone.get());

    }
    public void update(){
        if(driverController.getStartButton()){
            compressorEnableReq = true;
            compressorDisableReq = false;
        } else if(driverController.getBackButton()) {
            compressorEnableReq = false;
            compressorDisableReq = true;
        } else {
            compressorEnableReq = false;
            compressorDisableReq = false;
        }



    }
    public boolean getCompressorDisableReq() {
        return this.compressorDisableReq;
    }

    public boolean getCompressorEnableReq() {
        return this.compressorEnableReq;
    }
    /**
     * Get the driver-commanded forward/reverse speed
     * @return 1.0 for full forward, -1.0 for full reverse
     */
    public double getFwdRevCmd(){
        return Utils.ctrlAxisScale(-1.0*driverController.getY(GenericHID.Hand.kLeft), 4.0, 0.15); 
    }

        /**
     * Get the driver-commanded rotation
     * @return -1.0 for clockwise, 1.0 for counter-clockwise
     */
    public double getRotateCmd(){
        return Utils.ctrlAxisScale(-1.0*driverController.getX(GenericHID.Hand.kRight), 5.0, 0.15); 
    }

    public boolean getAutoHighGoalAlignDesired(){
        return driverController.getXButtonPressed(); 
 
    }
    
    public boolean getPhotonCannonInput(){
        return driverController.getYButton();
    }

    public boolean getDesiredBButtonCommand(){
        return driverController.getBButtonPressed();
    }

    public boolean getDesiredAButtonCommand(){
        return driverController.getAButtonPressed();

    }

    public boolean getDesiredLeftJoystickButtonCommand(){
        return driverController.getStickButtonPressed(Hand.kLeft);
    }
    
    public boolean getDesiredRightJoystickButtonCommand(){
        return driverController.getStickButtonPressed(Hand.kRight);
    }

    



}