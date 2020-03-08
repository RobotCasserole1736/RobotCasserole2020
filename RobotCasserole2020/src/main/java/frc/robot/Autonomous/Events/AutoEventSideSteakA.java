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
public class AutoEventSideSteakA extends AutoEvent {
    PathPlannerAutoEvent driveForward;

    //stuff for Intake
	double intkPrepDuration_s;
	double intkPrepEndTime;
	boolean intkPrepCompleted = true;

    // private final Waypoint[] waypoints_ft = new Waypoint[] {
    //     new Waypoint(0,      0,  Pathfinder.d2r(0)),
    //     new Waypoint(4.25,   -1.6,  Pathfinder.d2r(37))
    // };

    private final Waypoint[] waypoints_ft = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(12,   0,  Pathfinder.d2r(0)),
        new Waypoint(15,2.5,Pathfinder.d2r(-105))
    };

    public AutoEventSideSteakA(double intkPrepDuration_s_in) {
        intkPrepDuration_s = intkPrepDuration_s_in;
        driveForward = new PathPlannerAutoEvent(waypoints_ft, false,7,6);
    }

    @Override
    public void userStart() {
        intkPrepEndTime = Timer.getFPGATimestamp() + intkPrepDuration_s;
        intkPrepCompleted = false;
        Supperstructure.getInstance().setIntakeDesired(true);
        Supperstructure.getInstance().setPrepToShootDesired(true);
        
        driveForward.userStart();
    }

    @Override
    public void userUpdate() {
        intkPrepCompleted = (Timer.getFPGATimestamp() > intkPrepEndTime);
		if (intkPrepCompleted){
            Supperstructure.getInstance().setIntakeDesired(false);
            Supperstructure.getInstance().setPrepToShootDesired(false);
        }
        
        driveForward.userUpdate();
    }
    
    @Override
    public void userForceStop() {
        Supperstructure.getInstance().setIntakeDesired(false);
        Supperstructure.getInstance().setPrepToShootDesired(false);
        
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
        AutoEventSideSteakA autoEvent = new AutoEventSideSteakA(3.0); //time is for intk
		//TODO
		System.out.println("Done");
    }
}