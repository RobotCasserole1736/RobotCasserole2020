package frc.robot.Drivetrain;

import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;

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
 *    find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *    you have going on right now! We'd love to be able to help out! Shoot us 
 *    any questions you may have, all our contact info should be on our website
 *    (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *    Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *    if you would consider donating to our club to help further STEM education.
 */


 /**
 * 
 * The following coordinate system is used:
 * 
 *   Field Centerline
 *   |
 *   V 
 * 
 * 
 *   ^ Field Y+
 *   |                    _____________________________                             
 *   |                    |      Bumper Length        |                             
 *   |                    V                           V                             
 *   |                                                                              
 *   |                    =============================                       <-|
 *   |                    |    O       ^ Y+      O    |       <-|               |           
 *   |                    |            |              |         |               |   
 *   |                Rear|      Robot +----> X+      |Front    |Wheel Base     | Bumper Width
 *   |                    |                           |         |               |      
 *   |                    |    O                 O    |       <-|               |                  
 *   |                    =============================                       <-|     
 *   |                                                                              
 *   |                                                                              
 *   |                                                                              
 *   |                                                                              
 *<--+------------------------------------------------------------------> Field X+    <- Your alliance wall
 *   |
 *   V
 * Positive field Theta (T)   is defined as rotation from X+ to Y+. 0 Theta points along positive X axis. 
 * Field->Robot theta defined as angle from Field X axis to Robot X axis.
 * Robot drawn at T = 0deg.
 * 
 */

public class RobotPose {

    //Robot Physical Constants
    final double wheelRadius_In = RobotConstants.WHEEL_ROLLING_RADIUS_FT*12.0;
    final double BUMPER_WIDTH_FT = 2.0;
    final double SIDE_LINEAR_DISTANCE_PER_ROBOT_ROTATION_FT = RobotConstants.ROBOT_TRACK_WIDTH_FT*Math.PI; //account for wheel scrub on rotation here.
    final double BUMPER_LENGTH_FT = 2.5;

    //Field physical Constants  - model as a rectangle for now
    final double FIELD_UPPER_BOUNDARY_FT = 54.0;
    final double FIELD_LOWER_BOUNDARY_FT = 0.0;
    final double FIELD_LEFT_BOUNDARY_FT  = -13.47;
    final double FIELD_RIGHT_BOUNDARY_FT = 13.47;

    //Starting Position    
    public final double INIT_POSE_X = 0.0;
    public final double INIT_POSE_Y = 3.0;
    public final double INIT_POSE_T = 90;

    //Robot State
    public double leftWheelSpeed_RPM;
    public double rightWheelSpeed_RPM;
    public double poseX = INIT_POSE_X;
    public double poseY = INIT_POSE_Y;
    public double poseT = INIT_POSE_T;
    public double delta_y_robot_ft;
    public double delta_x_robot_ft;
    public double delta_t_robot_deg;
    public double poseAngle = 0;
    public boolean angleAvail = false;

    double desPoseX = INIT_POSE_X;
    double desPoseY = INIT_POSE_Y;
    double desPoseT = INIT_POSE_T;

    //Simulation Timing
    double prevLoopTime = 0;
    double delta_t_sec = RobotConstants.MAIN_LOOP_Ts;
    
    //Model Controls
    Calibration resetPos;


    Signal DesX;
    Signal DesY;
    Signal DesT;
    Signal ActX;
    Signal ActY;
    Signal ActT;
    

    public RobotPose() {
        DesX = new Signal("botDesPoseX", "ft");
        DesY = new Signal("botDesPoseY", "ft");
        DesT = new Signal("botDesPoseT", "deg");
        ActX = new Signal("botActPoseX", "ft");
        ActY = new Signal("botActPoseY", "ft");
        ActT = new Signal("botActPoseT", "deg");

        resetPos = new Calibration("Pose Calc Reset Position", 0, 0, 1);
    }

    public void setLeftMotorSpeed(double speed) {
        leftWheelSpeed_RPM = speed;
    }

    public void setRightMotorSpeed(double speed){
        rightWheelSpeed_RPM = speed;
    }
    
    public double getRobotVelocity_ftpersec(){
        return delta_y_robot_ft/delta_t_sec;
    }
    
    
    public void setMeasuredPoseAngle(double poseAngle_in, boolean angleAvailable_in) {
         angleAvail = angleAvailable_in;
         poseAngle   = poseAngle_in ;
    }

    public void updateFieldPoseFromRobotMotion(double deltaX_in, double deltaY_in, double deltaT_in){
        poseX += cos(poseT)*deltaX_in*delta_t_sec - sin(poseT)*deltaY_in*delta_t_sec;
        poseY += sin(poseT)*deltaX_in*delta_t_sec + cos(poseT)*deltaY_in*delta_t_sec;
        poseT += deltaT_in*delta_t_sec;
    }

    public double getRobotPoseAngleDeg(){
        return poseT;
    }


        
    
    public void update() {

        if(resetPos.get() == 1.0){
            reset();
        }
        
        updatePoseFromWheelSpeeds();
        handleFieldColission();

        double sample_time_ms = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;
        DesX.addSample(sample_time_ms,desPoseX);
        DesY.addSample(sample_time_ms,desPoseY);
        DesT.addSample(sample_time_ms,desPoseT);
        ActX.addSample(sample_time_ms,poseX);
        ActY.addSample(sample_time_ms,poseY);
        ActT.addSample(sample_time_ms,poseT);

    }
    
    public void reset() {
        poseX = INIT_POSE_X;
        poseY = INIT_POSE_Y;
        poseT = INIT_POSE_T;
        desPoseX = INIT_POSE_X;
        desPoseY = INIT_POSE_Y;
        desPoseT = INIT_POSE_T;
        leftWheelSpeed_RPM = 0;
        rightWheelSpeed_RPM = 0;
    }

    public void resetToPosition(double x_ft, double y_ft, double pose_angle_deg){
        poseX = x_ft;
        poseY = y_ft;
        poseT = pose_angle_deg;
        desPoseX = x_ft;
        desPoseY = y_ft;
        desPoseT = pose_angle_deg;
        leftWheelSpeed_RPM = 0;
        rightWheelSpeed_RPM = 0;
    }

    public void setDesiredPose(double x_ft, double y_ft, double pose_angle_deg){
        desPoseX = x_ft;
        desPoseY = y_ft;
        desPoseT = pose_angle_deg;
    }

    private void updatePoseFromWheelSpeeds(){
        delta_t_sec = LoopTiming.getInstance().getPeriodSec();

        //Robot frome velocity
        double leftVelocity_FPS = Utils.RPM_TO_FT_PER_SEC(leftWheelSpeed_RPM);
        double rightVelocity_FPS = Utils.RPM_TO_FT_PER_SEC(rightWheelSpeed_RPM);
        
        //Tank-drive robot frame displacement
        delta_y_robot_ft  = 0;
        delta_x_robot_ft  = (leftVelocity_FPS + rightVelocity_FPS)/2 *delta_t_sec;
        delta_t_robot_deg = ((-1.0 * leftVelocity_FPS) + rightVelocity_FPS)/2 * delta_t_sec * (1/SIDE_LINEAR_DISTANCE_PER_ROBOT_ROTATION_FT) * 360.0;
        
        //Transform to field coordinates
        poseX += cos(poseT)*delta_x_robot_ft + -1.0*sin(poseT)*delta_y_robot_ft;
        poseY += sin(poseT)*delta_x_robot_ft +      cos(poseT)*delta_y_robot_ft;

        if (angleAvail){
            poseT = poseAngle;
        } else {
            poseT += delta_t_robot_deg;
        }
    }

    private void handleFieldColission(){

        //Helper calculations for distance from robot center out to sides
        final double dx = BUMPER_WIDTH_FT/2.0;
        final double dy = BUMPER_LENGTH_FT/2.0;
        final double ndx = -1.0*dx;
        final double ndy = -1.0*dy;

        //Calculate verticie locations using 2d rotation formulae https://academo.org/demos/rotation-about-point/
        double FL_Corner_X = poseX + ( ndx*cos(poseT) -  dy*sin(poseT) );
        double FL_Corner_Y = poseY + (  dy*cos(poseT) + ndx*sin(poseT) );
        double FR_Corner_X = poseX + (  dx*cos(poseT) -  dy*sin(poseT) );
        double FR_Corner_Y = poseY + (  dy*cos(poseT) +  dx*sin(poseT) );
        double RL_Corner_X = poseX + ( ndx*cos(poseT) - ndy*sin(poseT) );
        double RL_Corner_Y = poseY + ( ndy*cos(poseT) + ndx*sin(poseT) );
        double RR_Corner_X = poseX + (  dx*cos(poseT) - ndy*sin(poseT) );
        double RR_Corner_Y = poseY + ( ndy*cos(poseT) +  dx*sin(poseT) );
 
        //The extrema of the verticiecs forms the bounding box of the robot
        double robotFrontBounds = max4(FL_Corner_Y, FR_Corner_Y, RL_Corner_Y, RR_Corner_Y);
        double robotRearBounds  = min4(FL_Corner_Y, FR_Corner_Y, RL_Corner_Y, RR_Corner_Y);
        double robotRightBounds = max4(FL_Corner_X, FR_Corner_X, RL_Corner_X, RR_Corner_X);
        double robotLeftBounds  = min4(FL_Corner_X, FR_Corner_X, RL_Corner_X, RR_Corner_X);

        //If the corresponding side of the boundary box exceeds the field boundary, the robot is in colission with a wall.

        if(robotFrontBounds > FIELD_UPPER_BOUNDARY_FT){
            //Robot colliding with opposing alliance wall
            poseY -= (robotFrontBounds - FIELD_UPPER_BOUNDARY_FT); //Reset bot within field
            //System.out.println("Colission with upper field boundary");
        }

        if(robotRearBounds < FIELD_LOWER_BOUNDARY_FT){
            //Robot colliding with your alliance wall
            poseY += (FIELD_LOWER_BOUNDARY_FT - robotRearBounds); //Reset bot within field
            //System.out.println("Colission with Lower field boundary");
        }

        if(robotRightBounds > FIELD_RIGHT_BOUNDARY_FT){
            //Robot colliding with opposing alliance wall
            poseX -= (robotRightBounds - FIELD_RIGHT_BOUNDARY_FT); //Reset bot within field
            //System.out.println("Colission with Right field boundary");
        }

        if(robotLeftBounds < FIELD_LEFT_BOUNDARY_FT){
            //Robot colliding with opposing alliance wall
            poseX += (FIELD_LEFT_BOUNDARY_FT - robotLeftBounds); //Reset bot within field
            //System.out.println("Colission with Left field boundary");
        }
    }



    //Utility Math helper functions
    private double cos(double in_deg){
        return Math.cos(in_deg*Math.PI/180.0);
    }

    private double sin(double in_deg){
        return Math.sin(in_deg*Math.PI/180.0);
    }

    private double max4(double in1, double in2, double in3, double in4){
        return Math.max(in1, Math.max(in2, Math.max(in3, in4)));
    }

    private double min4(double in1, double in2, double in3, double in4){
        return Math.min(in1, Math.min(in2, Math.min(in3, in4)));
    }
}