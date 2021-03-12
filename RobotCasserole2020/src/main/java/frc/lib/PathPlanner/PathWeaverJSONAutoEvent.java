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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.trajectory.TrajectoryUtil;
import edu.wpi.first.wpilibj.trajectory.Trajectory.State;
import edu.wpi.first.wpilibj.util.Units;

/**
 * Interface into the Casserole autonomous sequencer for a path-planned traversal. Simply wraps
 * path-planner functionality into the AutoEvent abstract class.
 */

public class PathWeaverJSONAutoEvent extends AutoEvent {

    /* Path planner wrapped by this auto event */
    boolean pathCalculated = false;

    boolean done = false;


    private int timestep;
    private double taskRate = RobotConstants.MAIN_LOOP_Ts;
    private final double DT_TRACK_WIDTH_FT = RobotConstants.ROBOT_TRACK_WIDTH_FT; // Width in Feet

    double desStartX = 0;
    double desStartY = 0;
    double desStartT = 0;

    Trajectory trajectory;


    public PathWeaverJSONAutoEvent(String jsonFileName, double maxVel, double maxAccel, double trackWidthFactor) {
        final String resourceBaseLocal = "./src/main/deploy/pathData";
        final String resourceBaseRIO = "/home/lvuser/deploy/pathData";
        String resourceBase = resourceBaseRIO; // default to roboRIO
    
        // Check if the path for resources expected on the roboRIO exists.
        if (Files.exists(Paths.get(resourceBaseRIO))) {
            // If RIO path takes priority (aka we're running on a roborio) this path takes
            // priority
            resourceBase = resourceBaseRIO;
        } else {
            // Otherwise use a local path, like we're running on a local machine.
            resourceBase = resourceBaseLocal;
        }

        Trajectory basePointsTraj = new Trajectory();
        try{
            basePointsTraj = TrajectoryUtil.fromPathweaverJson(Path.of(resourceBase, jsonFileName));
        } catch (IOException ex) {
            DriverStation.reportError("Unable to open trajectory: " + jsonFileName, ex.getStackTrace());
        }

        TrajectoryConfig cfg = new TrajectoryConfig(Units.feetToMeters(maxVel), Units.feetToMeters(maxAccel));
        DifferentialDriveKinematics kinematics = new DifferentialDriveKinematics(Units.feetToMeters(trackWidthFactor*DT_TRACK_WIDTH_FT));
        cfg.setKinematics(kinematics);

        //Strip out poses from the input trajectory
        ArrayList <Pose2d> poseList = new ArrayList<Pose2d>();
        for(State state : basePointsTraj.getStates() ){
            poseList.add(state.poseMeters);
        }
        
        //Generate a new trajectory with the correct robot drivetrain parameters config
        trajectory = TrajectoryGenerator.generateTrajectory(poseList, cfg);


    }

    /**
     * On the first loop, calculates velocities needed to take the path specified. Later loops will
     * assign these velocities to the drivetrain at the proper time.
     */
    double startTime = 0;
    double startPoseAngle = 0;
    double poseCommandPrev_deg = startPoseAngle;
    double poseCmdRevOffset = 0;
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


        double trajPoseCmd = trj_center.get(timestep).heading;
        double leftCommand_RPM  = FT_PER_SEC_TO_WHEEL_RPM(trj_left.get(timestep).velocity);
        double rightCommand_RPM = FT_PER_SEC_TO_WHEEL_RPM(trj_right.get(timestep).velocity); 
        double poseCommand_deg  = (Pathfinder.r2d(trj_center.get(1).heading - trajPoseCmd));
        double desX = trj_center.get(timestep).y; //Hurray for subtle and undocumented reference frame conversions.
        double desY = trj_center.get(timestep).x; //Hurray for subtle and undocumented reference frame conversions.
        
        //Sanitize the pathplanner headding command to be continous
        // When the loop-to-loop pose command jumps more than 170 degrees, add (or subtract) 360 to help offset that.
        double delta =  (poseCommand_deg+ poseCmdRevOffset) - poseCommandPrev_deg;
        if(delta > 170){
            poseCmdRevOffset -= 360;
        } else if (delta < -170){
            poseCmdRevOffset += 360;
        }
        poseCommand_deg += poseCmdRevOffset;
        poseCommandPrev_deg = poseCommand_deg;


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
        poseCommandPrev_deg = startPoseAngle;
        done = false;
    }
    
    private double FT_PER_SEC_TO_WHEEL_RPM(double ftps_in) {
        return ftps_in / (2*Math.PI*Drivetrain.WHEEL_ROLLING_RADIUS_FT) * 60;
    } 


}