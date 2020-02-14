package frc.robot;

import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import frc.lib.DataServer.Signal;

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

    Signal sideTiltAngleSig;
    Signal frontTiltAngleSig;
    Signal totalRobotTiltAngleSig;

    double totalRobotAngle;
    double sideTiltAngle;
    double frontTiltAngle;

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private RobotTilt() {
        onboardAccel = new BuiltInAccelerometer();
        sideTiltAngleSig = new Signal("Robot Side Tilt Angle", "degrees");
        sideTiltAngleSig = new Signal("Robot Front Tilt Angle", "degrees");

    }

    public void update(){
        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;

        double forceInXDirection; //X refers to the left and right sides of the robot
        double forceInZDirection; //Z refers to the up and down forces with the robot
        double forceInYDirection; //Y refers to the front to back of the robot

        forceInXDirection = onboardAccel.getX();
        forceInZDirection = onboardAccel.getZ();
        forceInYDirection = onboardAccel.getY();
        
        //Calculates the angle our robot is at by doing the arc tan of the force in the z direction over the force in the x direction
        sideTiltAngle = Math.toDegrees(Math.atan2(forceInZDirection, forceInXDirection));

        //Calculates angle from the front of the robot.
        //frontTiltAngle = Math.toDegrees(Math.atan2(forceInZDirection, forceInYDirection));

        //adding 150 is to make it equal 0 when the robot is upright. Why this is needed I have no idea.
        totalRobotAngle = sideTiltAngle+90;

        sideTiltAngleSig.addSample(sampleTimeMs, sideTiltAngle);
        //frontTiltAngleSig.addSample(sampleTimeMs, sideTiltAngle);
        totalRobotTiltAngleSig.addSample(sampleTimeMs, sideTiltAngle);
    }
    
    public double getRobotAngle(){
        return totalRobotAngle;
    }
}