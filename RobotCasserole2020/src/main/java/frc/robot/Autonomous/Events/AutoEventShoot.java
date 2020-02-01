package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Supperstructure;

public class AutoEventShoot extends AutoEvent {
	
	double speed;
	double duration_s;
	double endTime;
	boolean completed = true;
	
	public AutoEventShoot(double duration_s_in) {
		duration_s = duration_s_in;
	}

	@Override
	public void userStart() {
		endTime = Timer.getFPGATimestamp() + duration_s;
        completed = false;
		Supperstructure.getInstance().setShootDesired(true);
	}
	
	@Override
	public void userUpdate() {
		completed = (Timer.getFPGATimestamp() > endTime);
		if (completed){
			Supperstructure.getInstance().setShootDesired(false);
		}
	}

	@Override
	public void userForceStop() {
		Supperstructure.getInstance().setShootDesired(false);
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
