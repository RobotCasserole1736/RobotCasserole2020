package frc.robot.Autonomous.Events;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.FalconPathPlanner;
import frc.lib.PathPlanner.PathPlannerAutoEvent;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Waypoint;

/**
 * drive straight and stuff. Step response check (with typical smoothing)
 */
public class AutoEventCollectSteak extends AutoEvent {
    PathPlannerAutoEvent driveBackward;

    //Waypoints always start at (0,0), and are referenced relative to the robot's
    // position and pose angle whenever the event starts running. Units must be inches.

    private final Waypoint[] waypoints_ft = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(11, 0,  Pathfinder.d2r(0))
    };

    public AutoEventCollectSteak() {
        driveBackward = new PathPlannerAutoEvent(waypoints_ft, true);
    }

    @Override
    public void userUpdate() {
        driveBackward.userUpdate();
    }

    @Override
    public void userForceStop() {
        driveBackward.userForceStop();
    }

    @Override
    public boolean isTriggered() {
        return driveBackward.isTriggered();
    }

    @Override
    public boolean isDone() {
        return driveBackward.isDone();
    }

    @Override
    public void userStart() {
        driveBackward.userStart();
    }
    
    public static void main(String[] args) {
		System.out.println("Starting path planner calculation...");
        AutoEventCollectSteak autoEvent = new AutoEventCollectSteak();
		//TODO
		System.out.println("Done");
    }
}