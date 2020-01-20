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
    private double time_duration_s; 
    boolean pathCalculated = false;
    boolean reversed = false;
    
    boolean done = false;

    Trajectory trj_center;
    Trajectory trj_left;
    Trajectory trj_right;

    private int timestep;
    private double taskRate = 0.02;
    private final double DT_TRACK_WIDTH_FT = 25.0 / 12.0; //Width in Feet
    
    //Special mode for supporting two-cube auto
    // Will lock-in a given heading at the start of the path execution,
    boolean useFixedHeadingMode = false;
    double userManualHeadingDesired = 0;

    
    /**
     * Constructor. Set up the parameters of the planner here.
     * 
     * @param waypoints_in Set of x/y points which define the path the robot should take. In Inches.
     * @param timeAllowed_in Number of seconds the path traversal should take. Must be long enough
     *        to allow the path planner to output realistic speeds.         
     */
    public PathPlannerAutoEvent(Waypoint[] waypoints_in, double timeAllowed_in) { 
    	super();
    	commonConstructor(waypoints_in, timeAllowed_in, false, 0.2, 0.5, 0.01, 0.9);
    }
    
    /**
     * Constructor. Set up the parameters of the planner here.
     * 
     * @param waypoints_in Set of x/y points which define the path the robot should take. Assumes Inches
     * @param timeAllowed_in Number of seconds the path traversal should take. Must be long enough
     *        to allow the path planner to output realistic speeds. 
     * @param reversed set to True if you desire the robot to travel backward through the provided path        
     */
    public PathPlannerAutoEvent(Waypoint[] waypoints_in, double timeAllowed_in, boolean reversed_in) {        
    	super();
    	commonConstructor(waypoints_in, timeAllowed_in, reversed_in, 0.2, 0.5, 0.01, 0.9);

    }
    
    
    /**
     * Constructor. Set up the parameters of the planner here.
     * 
     * @param waypoints_in Set of x/y points which define the path the robot should take. Assumes Inches
     * @param timeAllowed_in Number of seconds the path traversal should take. Must be long enough
     *        to allow the path planner to output realistic speeds. 
     * @param reversed set to True if you desire the robot to travel backward through the provided path        
     */
    public PathPlannerAutoEvent(Waypoint[] waypoints_in, double timeAllowed_in, boolean reversed_in, double alpha, double beta, double valpha, double vbeta) {        
    	super();
    	commonConstructor(waypoints_in, timeAllowed_in, reversed_in, alpha, beta, valpha, vbeta);

    }
    
    private void commonConstructor(Waypoint[] waypoints_in, double timeAllowed_in, boolean reversed_in, double alpha, double beta, double valpha, double vbeta) {
        waypoints = waypoints_in;
        time_duration_s = timeAllowed_in;
        reversed = reversed_in;
        
        Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, 
                                                         Trajectory.Config.SAMPLES_HIGH, 
                                                         RobotConstants.MAIN_LOOP_SAMPLE_RATE_S, 
                                                         3.5, //Max Vel (m/s)
                                                         1.5, //Max Accel (m/s2)
                                                         60.0); //Max Jerk (m/s3)

        trj_center = Pathfinder.generate(waypoints, config);

        //Transform robot center trajectory to left/right wheel velocities.
        TankModifier modifier = new TankModifier(trj_center);
        modifier.modify(DT_TRACK_WIDTH_FT);
        trj_left  = modifier.getLeftTrajectory();       // Get the Left Side
        trj_right = modifier.getRightTrajectory();      // Get the Right Side
        
    }
    /**
     * On the first loop, calculates velocities needed to take the path specified. Later loops will
     * assign these velocities to the drivetrain at the proper time.
     */
    double startTime = 0;
    double startPoseAngle = 0;
    public void userUpdate() {
    	double tmp;
        
        //For _when_ loop timing isn't exact 20ms, and we need to skip setpoints,
        // calculate the proper timestep based on FPGA timestamp.
        tmp = (Timer.getFPGATimestamp()-startTime)/taskRate;
        timestep = (int) Math.round(tmp);
        
        if(timestep >= path.numFinalPoints) {
        	timestep = (int) (path.numFinalPoints - 1);
        	done = true;
        }
        
        //Be sure we skip the first timestep. The planner produces a bogus all-zeros point for it
        if (timestep == 0) {
        	timestep = 1;
        }

        
        //Interpret the path planner outputs into commands which are meaningful.
        double leftCommand_RPM  = 0;
        double rightCommand_RPM = 0;
        double poseCommand_deg  = 0;
        
        if(reversed) {
        	//When running in reversed mode, we need to undo the inversion applied to the 
        	// the waypoints.
            leftCommand_RPM  = -1*FT_PER_SEC_TO_WHEEL_RPM(path.smoothRightVelocity[timestep][1]);
            rightCommand_RPM = -1*FT_PER_SEC_TO_WHEEL_RPM(path.smoothLeftVelocity[timestep][1]);
        } else {
            leftCommand_RPM  = FT_PER_SEC_TO_WHEEL_RPM(path.smoothLeftVelocity[timestep][1]);
            rightCommand_RPM = FT_PER_SEC_TO_WHEEL_RPM(path.smoothRightVelocity[timestep][1]); 
        }
        poseCommand_deg  = (path.heading[timestep][1]-90.0) + startPoseAngle;
        
        if(useFixedHeadingMode) {
            Drivetrain.getInstance().setClosedLoopSpeedCmd(leftCommand_RPM, rightCommand_RPM, userManualHeadingDesired);
        } else {
            Drivetrain.getInstance().setClosedLoopSpeedCmd(leftCommand_RPM, rightCommand_RPM, poseCommand_deg);
        }

        Drivetrain.getInstance().autoTimestamp = timestep;
        Drivetrain.getInstance().leftAutoCmdFtPerSec = path.smoothLeftVelocity[timestep][1];
        Drivetrain.getInstance().rightAutoCmdFtPerSec = path.smoothRightVelocity[timestep][1];
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
		if (pathCalculated == false) {
            path.calculate(time_duration_s, taskRate, DT_TRACK_WIDTH_FT);
            timestep = 0;
            pathCalculated = true;
		}
		
        startTime = Timer.getFPGATimestamp();
        startPoseAngle = Drivetrain.getInstance().getGyroAngle();
        done = false;
	}
	
	private double FT_PER_SEC_TO_WHEEL_RPM(double ftps_in) {
		return ftps_in / (2*Math.PI*Drivetrain.WHEEL_ROLLING_RADIUS_FT) * 60;
	} 


}