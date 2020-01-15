package frc.robot.Autonomous.Events;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.FalconPathPlanner;
import frc.lib.PathPlanner.PathPlannerAutoEvent;

/**
 * drive straight and stuff. Step response check (with typical smoothing)
 */
public class AutoEventBackUpFromBallThief extends AutoEvent {
	PathPlannerAutoEvent driveBackward;

	//Waypoints always start at (0,0), and are referenced relative to the robot's
	// position and pose angle whenever the event starts running. Units must be inches.
	//private final double[][] waypoints_inches = new double[][] {
	//	{0,0},
    //    {0, -32.5},
	//	{-96, -32.5},
	//	{-96, -132},
	//	{-132, -132}
	//};

	private final double[][] waypoints_inches = new double[][] {
		{0,0},
        {0, -30},
        {-132, -112},
        {-132, -132},
	};

    final double time = 3.0;

	public AutoEventBackUpFromBallThief() {
		driveBackward = new PathPlannerAutoEvent(waypoints_inches, time, true ,0.9, 0.03, 0.01, 0.9);
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
    	AutoEventBackUpFromBallThief autoEvent = new AutoEventBackUpFromBallThief();
		FalconPathPlanner.plotPath(autoEvent.driveBackward.path);
	}
}