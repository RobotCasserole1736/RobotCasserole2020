package frc.lib.PathPlanner;

/*
 *******************************************************************************************
 * Copyright (C) 2017 FRC Team 1736 Robot Casserole - www.robotcasserole.org
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

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.Util.CrashTracker;
import frc.robot.RobotConstants;
import frc.robot.Drivetrain.Drivetrain;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.modifiers.TankModifier;
import edu.wpi.first.wpilibj.Timer;

/**
 * Interface into the Casserole autonomous sequencer for a path-planned traversal. Simply wraps
 * path-planner functionality into the AutoEvent abstract class.
 */

public class PathPlannerAutoEvent extends AutoEvent {

    /* Path planner wrapped by this auto event */
    private Waypoint[] waypoints;
    boolean pathCalculated = false;
    boolean reversed = false;

    boolean done = false;

    Trajectory trj_center;
    Trajectory trj_left;
    Trajectory trj_right;

    private int timestep;
    private double taskRate = RobotConstants.MAIN_LOOP_Ts;
    private final double DT_TRACK_WIDTH_FT = RobotConstants.ROBOT_TRACK_WIDTH_FT; //Width in Feet
    
    //Special mode for supporting two-cube auto
    // Will lock-in a given heading at the start of the path execution,
    boolean useFixedHeadingMode = false;
    double userManualHeadingDesired = 0;

    double desStartX = 0;
    double desStartY = 0;
    double desStartT = 0;

    public PathPlannerAutoEvent(Waypoint[] waypoints_in, boolean reversed_in) {
        commonConstructor(waypoints_in, reversed_in, 8.0, 6.0); //Default for max speed (ft/sec), and max accesl ()
    }

    public PathPlannerAutoEvent(Waypoint[] waypoints_in, boolean reversed_in, double maxVel, double maxAccel){
        commonConstructor(waypoints_in, reversed_in, maxVel, maxAccel);
    }

    private void commonConstructor(Waypoint[] waypoints_in, boolean reversed_in, double maxVel, double maxAccel) {
        waypoints = waypoints_in;
        reversed = reversed_in;
        
        Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, 
                                                         Trajectory.Config.SAMPLES_FAST, 
                                                         taskRate, 
                                                         maxVel, //Max Vel (ft/sec)
                                                         maxAccel, //Max Accel (ft/sec2)
                                                         180.0); //Max Jerk (ft/sec3)

        if(reversed_in){
            for(Waypoint wpt: waypoints){
                wpt.x *= -1;
                wpt.y *= -1;
            }
        }

        try{
            trj_center = Pathfinder.generate(waypoints, config);

            //Transform robot center trajectory to left/right wheel velocities.
            TankModifier modifier = new TankModifier(trj_center);
            modifier.modify(DT_TRACK_WIDTH_FT);
            trj_left  = modifier.getRightTrajectory();     // Get the Left Side  (Yes, pathplanner is inverted compared to what we want)
            trj_right = modifier.getLeftTrajectory();      // Get the Right Side (Yes, pathplanner is inverted compared to what we want)
        } catch(Exception e) {
            CrashTracker.logGenericMessage("[Auto Path Planner]: Could not create auto path");
            CrashTracker.logGenericMessage(e.toString());
            trj_center = null;
            trj_left = null;
            trj_right = null;
        }
    
    }
    /**
     * On the first loop, calculates velocities needed to take the path specified. Later loops will
     * assign these velocities to the drivetrain at the proper time.
     */
    double startTime = 0;
    double startPoseAngle = 0;
    public void userUpdate() {

        if(trj_center == null){
            done = true;
            Drivetrain.getInstance().setClosedLoopSpeedCmd(0, 0);
            return; //no path, nothing to do.
        }

        //For _when_ loop timing isn't exact 20ms, and we need to skip setpoints,
        // calculate the proper timestep based on FPGA timestamp.
        double tmp = (Timer.getFPGATimestamp()-startTime)/taskRate;
        timestep = (int) Math.round(tmp);
        
        int maxTimestep = trj_center.length();

        if(timestep >= maxTimestep) {
            timestep = (maxTimestep - 1);
            done = true;
        }

        if(timestep == 0){
            timestep = 1; //Again, for some weird reason, step 0 is bogus?
        }

        double leftCommand_RPM  = FT_PER_SEC_TO_WHEEL_RPM(trj_left.get(timestep).velocity);
        double rightCommand_RPM = FT_PER_SEC_TO_WHEEL_RPM(trj_right.get(timestep).velocity); 
        double poseCommand_deg  = (Pathfinder.r2d(trj_center.get(1).heading - trj_center.get(timestep).heading));
        double desX = trj_center.get(timestep).y; //Hurray for subtle and undocumented reference frame conversions.
        double desY = trj_center.get(timestep).x; //Hurray for subtle and undocumented reference frame conversions.
        
        //UMMMM I guess pathplanner freaks out every now and then?
        if(poseCommand_deg > 180.0){
            poseCommand_deg -= 360.0;
        } else if (poseCommand_deg < -180.0 ) {
            poseCommand_deg += 360.0;
        }

        if(reversed){
            leftCommand_RPM  *= -1;
            rightCommand_RPM *= -1;

            double tmpSpdCmd = leftCommand_RPM;
            leftCommand_RPM = rightCommand_RPM;
            rightCommand_RPM = tmpSpdCmd;

            desX *= -1;
            desY *= -1;
        }

        //Rotate to the reference frame where we started the path plan event
        poseCommand_deg += startPoseAngle;
        if(!done){
            if(useFixedHeadingMode) {
                Drivetrain.getInstance().setClosedLoopSpeedCmd(leftCommand_RPM, rightCommand_RPM, userManualHeadingDesired);
            } else {
                Drivetrain.getInstance().setClosedLoopSpeedCmd(leftCommand_RPM, rightCommand_RPM, poseCommand_deg);
            }
        }else{
            Drivetrain.getInstance().setClosedLoopSpeedCmd(0, 0);
        }

        double simBotPoseT = poseCommand_deg;
        double simBotPoseX = desStartX + (Math.sin(Math.toRadians(startPoseAngle))) * desX + (Math.cos(Math.toRadians(startPoseAngle)) * desY);
        double simBotPoseY = desStartY - (Math.cos(Math.toRadians(startPoseAngle))) * desX + (Math.sin(Math.toRadians(startPoseAngle)) * desY);

        Drivetrain.getInstance().dtPose.setDesiredPose( simBotPoseX,
                                                        simBotPoseY, 
                                                        simBotPoseT);
    }


    /**
     * Force both sides of the drivetrain to zero
     */
    public void userForceStop() {
        Drivetrain.getInstance().setOpenLoopCmd(0, 0);
    }


    /**
     * Always returns true, since the routine should run as soon as it comes up in the list.
     */
    public boolean isTriggered() {
        return true; // we're always ready to go
    }


    /**
     * Returns true once we've run the whole path
     */
    public boolean isDone() {
        return done;
    }
    
    /**
     * Manually set what the heading should be - useful if you moved the robot
     * without the pathplanner's knowledge.
     */
    public void setDesiredHeadingOverride(double heading) {
        userManualHeadingDesired = heading;
        useFixedHeadingMode = true;
    }


    @Override
    public void userStart() {
        startTime = Timer.getFPGATimestamp();
        startPoseAngle = Drivetrain.getInstance().getGyroAngle();
        desStartX      = Drivetrain.getInstance().dtPose.poseX;
        desStartY      = Drivetrain.getInstance().dtPose.poseY;
        desStartT      = Drivetrain.getInstance().dtPose.poseT;
        done = false;
    }
    
    private double FT_PER_SEC_TO_WHEEL_RPM(double ftps_in) {
        return ftps_in / (2*Math.PI*Drivetrain.WHEEL_ROLLING_RADIUS_FT) * 60;
    } 


}