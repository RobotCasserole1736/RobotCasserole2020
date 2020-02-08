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
public class AutoEventCollectSteak extends AutoEvent {
    PathPlannerAutoEvent driveForward;

    //stuff for Intake
    double intkSpeed;
	double intkDuration_s;
	double intkEndTime;
	boolean intkCompleted = true;

    private final Waypoint[] waypoints_ft = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(3,   0,  Pathfinder.d2r(0))
    };

    public AutoEventCollectSteak(double intkDuration_s_in) {
        intkDuration_s = intkDuration_s_in;
        driveForward = new PathPlannerAutoEvent(waypoints_ft, false);
    }

    @Override
    public void userStart() {
        intkEndTime = Timer.getFPGATimestamp() + intkDuration_s;
        intkCompleted = false;
        Supperstructure.getInstance().setIntakeDesired(true);
        
        driveForward.userStart();
    }

    @Override
    public void userUpdate() {
        intkCompleted = (Timer.getFPGATimestamp() > intkEndTime);
		if (intkCompleted){
			Supperstructure.getInstance().setIntakeDesired(false);
        }
        
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
        AutoEventCollectSteak autoEvent = new AutoEventCollectSteak(4.0); //time is for intk
		//TODO
		System.out.println("Done");
    }
}