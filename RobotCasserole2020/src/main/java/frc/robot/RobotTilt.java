package frc.robot;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;

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

public class RobotTilt {
	private static RobotTilt empty = null;

	public static synchronized RobotTilt getInstance() {
		if(empty == null)
			empty = new RobotTilt();
		return empty;
	}
    
    BuiltInAccelerometer onboardAccel;

    double robotAngle;

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private RobotTilt() {
        onboardAccel = new BuiltInAccelerometer();
    }

    public void update(){
        double forceInXDirection;
        double forceInZDirection;

        forceInXDirection = onboardAccel.getX();
        forceInZDirection = onboardAccel.getZ();

        //System.out.println("The force in the x direction is "+forceInXDirection);
        //System.out.println("The force in the y direction is "+forceInZDirection);
        
        //Calculates the angle our robot is at by doing the arc tan of the force in the y direction over the force in the x direction
        robotAngle = Math.atan2(forceInZDirection, forceInXDirection);
        //adding 180 is to make it positive
        robotAngle = Math.toDegrees(robotAngle)+180;

        //System.out.println("Robot angle is " + robotAngle);
    }
    
    public double getRobotAngle(){
        return robotAngle;
    }
}