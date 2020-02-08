package frc.robot.Autonomous.Events;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.PathPlannerAutoEvent;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Waypoint;

/**
 * go to scale on left.
 */
public class AutoEventReversePathPlanTest extends AutoEvent {
    PathPlannerAutoEvent driveForward;

    private final Waypoint[] waypoints_ft = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(-15.0,   0.0,  Pathfinder.d2r(0.0))
    };

    public AutoEventReversePathPlanTest() {
        driveForward = new PathPlannerAutoEvent(waypoints_ft, true, 10.0, 12.0);
    }

    @Override
    public void userUpdate() {
        driveForward.userUpdate();
    }
    @Override
    public void userForceStop() {
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

    @Override
    public void userStart() {
        driveForward.userStart();
    }
    public static void main(String[] args) {
        AutoEventPathPlanTest autoEvent = new AutoEventPathPlanTest();
        //TODO 
    }
}