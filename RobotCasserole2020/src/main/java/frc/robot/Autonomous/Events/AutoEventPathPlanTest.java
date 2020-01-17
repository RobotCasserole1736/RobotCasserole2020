package frc.robot.Autonomous.Events;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.FalconPathPlanner;
import frc.lib.PathPlanner.PathPlannerAutoEvent;

/**
 * go to scale on left.
 */
public class AutoEventPathPlanTest extends AutoEvent {
	PathPlannerAutoEvent driveForward;

	private final double[][] waypoints = new double[][] {
        {0, 0},
		{0, 60},
		{25,70},
		{25,100}
	};
	
	private final double time = 5.0;

	public AutoEventPathPlanTest() {
		driveForward = new PathPlannerAutoEvent(waypoints, time, false, 0.5, 0.5, 0.001, 0.9);
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
		FalconPathPlanner.plotPath(autoEvent.driveForward.path);
	}
}