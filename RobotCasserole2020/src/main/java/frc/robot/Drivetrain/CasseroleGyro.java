package frc.robot.Drivetrain;

import com.analog.adis16448.frc.ADIS16448_IMU;

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

public class CasseroleGyro {
	private static CasseroleGyro instance = null;

	public static synchronized CasseroleGyro getInstance() {
		if(instance == null)
		    instance = new CasseroleGyro();
		return instance;
    }
    
    private CasseroleGyro(){
        public static final ADIS16448_IMU imu = new ADIS16448_IMU();
        //TODO - add gyro init code  
    }

    public void update(){
        

        //TODO - add code to read all relevant info from the gyro, and update a Signal so it shows up on the website.
    }

    public void calibrate(){
        //TODO - add code to cause the gyro to calibrate
    }


    public double getAngleDeg(){
        return 0; 
        //TODO - Return the present pose angle of the robot in degrees, 
        // where "0" pointed toward the right wall of the field, 
        //"90" is pointed straight at the opposing alliance wall, and
        //"180" is pointed at the left wall of the field
        //"270" is pointed at you.
        //Angle should not "wrap" - ie, 356 is a valid angle that means "5 degrees past the right wall".
    }

}
