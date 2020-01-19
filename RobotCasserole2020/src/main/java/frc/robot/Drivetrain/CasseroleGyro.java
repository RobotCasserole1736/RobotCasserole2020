package frc.robot.Drivetrain;

import com.analog.adis16448.frc.ADIS16448_IMU;

import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;

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

public class CasseroleGyro {

    ADIS16448_IMU imu;

    Signal poseAngleSig;

    double angle_deg = 0;
    double angle_offset = 0;
    
    public CasseroleGyro(){
        imu = new ADIS16448_IMU();
        poseAngleSig = new Signal("DT_Pose_Angle", "deg");
    }

    public void update(){
        angle_deg = imu.getAngle() + angle_offset;
        poseAngleSig.addSample(LoopTiming.getInstance().getLoopStartTimeSec()*1000, angle_deg);
    }

    public void calibrate(){
        imu.calibrate();
    }


    public double getAngleDeg(){
        return angle_deg; 
        // where "0" pointed toward the right wall of the field, 
        //"90" is pointed straight at the opposing alliance wall, and
        //"180" is pointed at the left wall of the field
        //"270" is pointed at you.
        //Angle should not "wrap" - ie, 356 is a valid angle that means "5 degrees past the right wall".
    }

	public boolean isOnline() {
		return imu != null; //As long as it's init'ed, not much to to.
    }
    
    //Resets casseroleGyro to start outputting curAngle. All future angle outputs are referenced relative to this.
    public void setCurAngle(double curAngle) {
        angle_offset = curAngle - imu.getAngle();
    }

}
