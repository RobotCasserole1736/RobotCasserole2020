package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.PathPlannerAutoEvent;
import frc.robot.Supperstructure;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Waypoint;

/**
 * go to scale on left.
 */
public class AutoEventGalacticRedA extends AutoEvent {
    PathPlannerAutoEvent driveForward;

    //stuff for Intake
	double intkPrepDuration_s;
	double intkPrepEndTime;
	boolean intkPrepCompleted = true;

    private final Waypoint[] waypoints_ft = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)), //Point B1
        new Waypoint(0.762,   0.762,  Pathfinder.d2r(45)), //Point C3
        new Waypoint(0.762,   1.524,Pathfinder.d2r(-45)), //Point D5
        new Waypoint(-2.286,   0.762,Pathfinder.d2r(45)), //Point A6
        new Waypoint(0.762,   3.81,Pathfinder.d2r(45)) //Point B11
        
    };

    public AutoEventGalacticRedA(double intkPrepDuration_s_in) {
        intkPrepDuration_s = intkPrepDuration_s_in;
        driveForward = new PathPlannerAutoEvent(waypoints_ft, false,5,5);
    }

    @Override
    public void userStart() {
        intkPrepEndTime = Timer.getFPGATimestamp() + intkPrepDuration_s;
        intkPrepCompleted = false;
        Supperstructure.getInstance().setIntakeDesired(true);
        driveForward.userStart();
    }

    @Override
    public void userUpdate() {
        intkPrepCompleted = (Timer.getFPGATimestamp() > intkPrepEndTime);
        
        driveForward.userUpdate();
    }
    
    @Override
    public void userForceStop() {
        Supperstructure.getInstance().setIntakeDesired(false);
        
        driveForward.userForceStop();
    }

    @Override
    public boolean isTriggered() {
        return driveForward.isTriggered();
    }

    @Override
    public boolean isDone() {
        return driveForward.isDone();
    }

    public static void main(String[] args) {
        AutoEventBallThiefA autoEvent = new AutoEventBallThiefA(4.0); //time is for intk
		//TODO
		System.out.println("Done");
    }
}