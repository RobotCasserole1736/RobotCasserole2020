package frc.robot.Autonomous.Events;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.PathPlannerAutoEvent;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Waypoint;

/**
 * drive straight and stuff. Step response check (with typical smoothing)
 */
public class AutoEventDriveBarrelRun extends AutoEvent {
    PathPlannerAutoEvent pathPlanAutoEvent;

    // Waypoints always start at (0,0), and are referenced relative to the robot's
    // position and pose angle whenever the event starts running. Units must be
    // feet.

    //Positive rotation to the right, negative to the left
    //Positive X forward, Negative backward
    //Positive Y to the right, negative to the left.

    public AutoEventDriveBarrelRun() {
        final Waypoint[] waypoints_ft = new Waypoint[] { 
                new Waypoint(0,  0, Pathfinder.d2r(0)),
                new Waypoint(12, 3, Pathfinder.d2r(45)),
                new Waypoint(12, 6, Pathfinder.d2r(135)),
                new Waypoint(7,  6, Pathfinder.d2r(225)),
                new Waypoint(7,  3, Pathfinder.d2r(315)),
                new Waypoint(12, 1, Pathfinder.d2r(360)),
                new Waypoint(20, 1, Pathfinder.d2r(360)),
                new Waypoint(24, -2, Pathfinder.d2r(315)),
                new Waypoint(20, -5, Pathfinder.d2r(225)),
                new Waypoint(16, -5, Pathfinder.d2r(135)),
                new Waypoint(20, -2, Pathfinder.d2r(45)),
                new Waypoint(22, -1, Pathfinder.d2r(0)),
            };

            pathPlanAutoEvent = new PathPlannerAutoEvent(waypoints_ft, false, 6, 6);

    }

    @Override
    public void userStart() {
        pathPlanAutoEvent.userStart();
    }

    @Override
    public void userUpdate() {
        pathPlanAutoEvent.userUpdate();
    }

    @Override
    public void userForceStop() {
        pathPlanAutoEvent.userForceStop();
    }

    @Override
    public boolean isTriggered() {
        return pathPlanAutoEvent.isTriggered();
    }

    @Override
    public boolean isDone() {
        return pathPlanAutoEvent.isDone();
    }

    public static void main(String[] args) {
        System.out.println("Starting path planner calculation...");
        AutoEventDriveBarrelRun autoEvent = new AutoEventDriveBarrelRun();
		System.out.println("Done");
    }
}