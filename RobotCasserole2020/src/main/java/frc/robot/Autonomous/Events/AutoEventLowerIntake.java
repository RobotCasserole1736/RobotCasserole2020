package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Supperstructure;
import frc.robot.BallHandling.IntakeControl;

public class AutoEventLowerIntake extends AutoEvent {
	
	double speed;
	double duration_s;
	double endTime;
	boolean completed = true;
	
	public AutoEventLowerIntake(double duration_s_in) {
		duration_s = duration_s_in;
	}

	@Override
	public void userStart() {
		endTime = Timer.getFPGATimestamp() + duration_s;
        completed = false;
		IntakeControl.getInstance();
	}
	
	@Override
	public void userUpdate() {
		completed = (Timer.getFPGATimestamp() > endTime);
		if (completed){
			Supperstructure.getInstance().setIntakeDesired(false);
		}
	}

	@Override
	public void userForceStop() {
		Supperstructure.getInstance().setIntakeDesired(false);
	}

	@Override
	public boolean isTriggered() {
		return true;
	}

	@Override
	public boolean isDone() {
		return completed;
	}
}
