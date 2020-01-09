package frc.robot.HumanInterface;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.GenericHID;

/*
 *******************************************************************************************
 * Copyright (C) 2019 FRC Team 1736 Robot Casserole - www.robotcasserole.org
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

    public double getFwdRevCmd(){
        return driverController.getY(GenericHID.Hand.kLeft); 
    }

    public double getRotateCmd(){
        return driverController.getX(GenericHID.Hand.kRight); 
    }

    public boolean getAutoHighGoalAlignDesired(){
        return driverController.getXButtonPressed(); 

    }
    


}