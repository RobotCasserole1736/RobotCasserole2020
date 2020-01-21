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
    private double taskRate = RobotConstants.MAIN_LOOP_SAMPLE_RATE_S;
    private final double DT_TRACK_WIDTH_FT = RobotConstants.ROBOT_TRACK_WIDTH_FT; //Width in Feet
    
    //Special mode for supporting two-cube auto
    // Will lock-in a given heading at the start of the path execution,
    boolean useFixedHeadingMode = false;
    double userManualHeadingDesired = 0;

    public PathPlannerAutoEvent(Waypoint[] waypoints_in, boolean reversed_in) {
        waypoints = waypoints_in;
        reversed = reversed_in;
        
        Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, 
                                                         Trajectory.Config.SAMPLES_HIGH, 
                                                         taskRate, 
                                                         3.5, //Max Vel (m/s)
                                                         2.5, //Max Accel (m/s2)
                                                         60.0); //Max Jerk (m/s3)

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
            trj_left  = modifier.getLeftTrajectory();       // Get the Left Side
            trj_right = modifier.getRightTrajectory();      // Get the Right Side
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
            return; //no path, nothing to do.
        }

        //For _when_ loop timing isn't exact 20ms, and we need to skip setpoints,
        // calculate the proper timestep based on FPGA timestamp.
        double tmp = (Timer.getFPGATimestamp()-startTime)/taskRate;
        timestep = (int) Math.round(tmp);
        

        if(timestep >= trj_center.length()) {
            timestep = (int) (trj_center.length() - 1);
            done = true;
        }

        if(timestep == 0){
            timestep = 1; //Again, for some weird reason, step 0 is bogus?
        }

        double leftCommand_RPM  = FT_PER_SEC_TO_WHEEL_RPM(trj_left.get(timestep).velocity);
        double rightCommand_RPM = FT_PER_SEC_TO_WHEEL_RPM(trj_right.get(timestep).velocity); 
        double poseCommand_deg  = (Pathfinder.r2d(trj_center.get(timestep).heading));
        
        if(reversed){
            leftCommand_RPM  = -1.0 * leftCommand_RPM;
            rightCommand_RPM = -1.0 * rightCommand_RPM;
            poseCommand_deg  = -1.0 * poseCommand_deg;
        }

        //Rotate to the reference frame where we started the path plan event
        poseCommand_deg += startPoseAngle;

        if(useFixedHeadingMode) {
            Drivetrain.getInstance().setClosedLoopSpeedCmd(leftCommand_RPM, rightCommand_RPM, userManualHeadingDesired);
        } else {
            Drivetrain.getInstance().setClosedLoopSpeedCmd(leftCommand_RPM, rightCommand_RPM, poseCommand_deg);
        }
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
        done = false;
    }
    
    private double FT_PER_SEC_TO_WHEEL_RPM(double ftps_in) {
        return ftps_in / (2*Math.PI*Drivetrain.WHEEL_ROLLING_RADIUS_FT) * 60;
    } 


}