package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Supperstructure;
import frc.robot.ShooterControl.ShooterControl;

public class AutoEventShoot extends AutoEvent {
	
	double speed;
	double duration_s;
	double endTime;
	int ballCount;
	int startBall;
	boolean completed = true;
	
	public AutoEventShoot(double duration_s_in, int ballCount_in) {
		duration_s = duration_s_in;
		ballCount=ballCount_in;
	}

	@Override
	public void userStart() {
		endTime = Timer.getFPGATimestamp() + duration_s;
		completed = false;
		startBall=ShooterControl.getInstance().getShotCount();
		Supperstructure.getInstance().setShootDesired(true);
		Supperstructure.getInstance().setPrepToShootDesired(false); //because PrepToShoot overrides Shoot
	}
	
	@Override
	public void userUpdate() {
		completed = (Timer.getFPGATimestamp() > endTime);
		if (completed||ShooterControl.getInstance().getShotCount()-startBall>=ballCount){
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
