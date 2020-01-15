package frc.robot.Autonomous.Events;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.FalconPathPlanner;
import frc.lib.PathPlanner.PathPlannerAutoEvent;

/**
 * drive straight and stuff. Step response check (with typical smoothing)
 */
public class AutoEventBackUpFromBallThief extends AutoEvent {
	PathPlannerAutoEvent driveBackward;

	private final double[][] waypoints = new double[][] {
		{0,0},
        {0,-65},
        {-192, -65},
        {-192, -130}
	};
	
	private final double time = 1.5;

	public AutoEventBackUpFromBallThief() {
		driveBackward = new PathPlannerAutoEvent(waypoints, time, true, 0.2, 0.5, 0.001, 0.9);
	}

	@Override
	public void userUpdate() {
		driveBackward.userUpdate();
		// shotCTRL.setDesiredShooterState(ShooterStates.PREP_TO_SHOOT);
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