package frc.robot.Autonomous.Events;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.FalconPathPlanner;
import frc.lib.PathPlanner.PathPlannerAutoEvent;

/**
 * go to scale on left.
 */
public class AutoEventDriveToBallThief extends AutoEvent {
	PathPlannerAutoEvent driveForward;

	private final double[][] waypoints = new double[][] {
        {0, 0},
        {0, 130.36} //Puts front of robot right on the balls. If intake is further forward it may need to change.
	};
	
	private final double time = 2.0;

	public AutoEventDriveToBallThief() {
		driveForward = new PathPlannerAutoEvent(waypoints, time, false, 0.5, 0.5, 0.001, 0.9);
	}

	@Override
	public void userUpdate() {
		driveForward.userUpdate();
		// shotCTRL.setDesiredShooterState(ShooterStates.PREP_TO_SHOOT);
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
		AutoEventDriveToBallThief autoEvent = new AutoEventDriveToBallThief();
		FalconPathPlanner.plotPath(autoEvent.driveForward.path);
	}
}