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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    private double DT_TRACK_WIDTH_FT;

    double desStartX = 0;
    double desStartY = 0;
    double desStartT = 0;

    double trajStartX = 0;
    double trajStartY = 0;
    double trajStartT = 0;

    Trajectory trajectory;

    List<State> trajectoryStateList;


    public PathWeaverJSONAutoEvent(String jsonFileName, double maxVel, double maxAccel, double trackWidthFactor) {

        DT_TRACK_WIDTH_FT = RobotConstants.ROBOT_TRACK_WIDTH_FT*trackWidthFactor; // Width in Feet

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

        TrajectoryConfig cfg = new TrajectoryConfig(maxVel,maxAccel);
        DifferentialDriveKinematics kinematics = new DifferentialDriveKinematics(DT_TRACK_WIDTH_FT);
        cfg.setKinematics(kinematics);

        //Strip out poses from the input trajectory
        ArrayList <Pose2d> poseList = new ArrayList<Pose2d>();
        for(State state : basePointsTraj.getStates() ){
            poseList.add(state.poseMeters);
        }
        
        //Generate a new trajectory with the correct robot drivetrain parameters config
        trajectory = TrajectoryGenerator.generateTrajectory(poseList, cfg);

        trajectoryStateList = trajectory.getStates(); //Optimiztion - one-time save off states in a list.
    }

    /**
     * On the first loop, calculates velocities needed to take the path specified. Later loops will
     * assign these velocities to the drivetrain at the proper time.
     */
    double startTime = 0;
    double endTime = 0;
    double startPoseAngle = 0;
    int curStep = 0;
    double prevPoseCommand_deg;
    boolean curStepAdvanced = false;
    double prevDesHeadingRaw = 0;
    double poseOffsetDeg = 0;

    public void userUpdate() {


        double curTime = (Timer.getFPGATimestamp()-startTime);

        //Check for finish
        if(curTime >= trajectory.getTotalTimeSeconds() || curStep >= trajectoryStateList.size() ) {
            done = true;
            Drivetrain.getInstance().setClosedLoopSpeedCmd(0, 0);
            return;
        }

        //Advance to current timestep
        curStepAdvanced = false;
        while(trajectoryStateList.get(curStep).timeSeconds < curTime){
            curStepAdvanced = true;
            curStep++;
        }

        // Extract current and previous steps
        State curState = trajectoryStateList.get(curStep);

        //get the translational and rotational velocities desired in feet/sec
        double desTransVelFtPerSec = curState.velocityMetersPerSecond;

        //Massage the pose command from the trajectory to be continuous
        //Trajectory appears to output -180 to 180
        double curDesHeadingRaw = startPoseAngle + curState.poseMeters.getRotation().getDegrees() - trajStartT;

        while((curDesHeadingRaw - prevDesHeadingRaw) >= 180){
            curDesHeadingRaw -= 360;
        }

        while((curDesHeadingRaw - prevDesHeadingRaw) <= -180){
            curDesHeadingRaw += 360;
        }
        //Calculate where drivetrain ought to be pointed (for closed-loop gyro feedback)
        double poseCommand_deg = curDesHeadingRaw;

        //Curvature doesn't seem to be calculated correctly out of the trajectory library, so we make our own.
        double desRotVelFtPerSec = Units.degreesToRadians(poseCommand_deg - prevPoseCommand_deg) /0.02 * DT_TRACK_WIDTH_FT/2;

        //Convert to drivetrain speeds
        double leftCommand_mps  = desTransVelFtPerSec - desRotVelFtPerSec;
        double rightCommand_mps = desTransVelFtPerSec + desRotVelFtPerSec;


        Drivetrain.getInstance().setClosedLoopSpeedCmd(FT_PER_SEC_TO_WHEEL_RPM(leftCommand_mps), 
                                                       FT_PER_SEC_TO_WHEEL_RPM(rightCommand_mps), 
                                                        poseCommand_deg);


        //Populate desired pose from path plan.
        double desY = (curState.poseMeters.getTranslation().getX() - trajStartX);
        double desX = -1.0 * (curState.poseMeters.getTranslation().getY() - trajStartY);
        double simBotPoseT = poseCommand_deg;
        double simBotPoseX = desStartX + (Math.sin(Math.toRadians(startPoseAngle))) * desX + (Math.cos(Math.toRadians(startPoseAngle)) * desY);
        double simBotPoseY = desStartY - (Math.cos(Math.toRadians(startPoseAngle))) * desX + (Math.sin(Math.toRadians(startPoseAngle)) * desY);

        Drivetrain.getInstance().dtPose.setDesiredPose( simBotPoseX,
                                                        simBotPoseY, 
                                                        simBotPoseT);

        prevPoseCommand_deg = poseCommand_deg;
        prevDesHeadingRaw = curDesHeadingRaw;
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


    @Override
    public void userStart() {
        startPoseAngle = Drivetrain.getInstance().getGyroAngle();
        desStartX      = Drivetrain.getInstance().dtPose.poseX;
        desStartY      = Drivetrain.getInstance().dtPose.poseY;
        desStartT      = Drivetrain.getInstance().dtPose.poseT;

        trajStartX = trajectory.getInitialPose().getTranslation().getX();
        trajStartY = trajectory.getInitialPose().getTranslation().getY();
        trajStartT = trajectory.getInitialPose().getRotation().getDegrees();

        prevPoseCommand_deg = startPoseAngle;
        prevDesHeadingRaw = trajStartT;
        poseOffsetDeg = 0;

        done = false;
        startTime = Timer.getFPGATimestamp();
        endTime = startTime + trajectory.getTotalTimeSeconds();
    }
    
    private double FT_PER_SEC_TO_WHEEL_RPM(double ftps_in) {
        return ftps_in / (2*Math.PI*Drivetrain.WHEEL_ROLLING_RADIUS_FT) * 60;
    } 


}