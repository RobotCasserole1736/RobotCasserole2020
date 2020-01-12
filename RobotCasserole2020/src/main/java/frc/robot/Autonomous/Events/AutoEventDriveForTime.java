package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Drivetrain.Drivetrain;

public class AutoEventDriveForTime extends AutoEvent {
	
	double speed;
	double duration_s;
	double endTime;
	boolean completed = true;
	
	public AutoEventDriveForTime(double duration_s_in, double speed_in) {
		duration_s = duration_s_in;
		speed = speed_in;
	}

	@Override
	public void userStart() {
		endTime = Timer.getFPGATimestamp() + duration_s;
		completed = false;
	}

	@Override
	public void userUpdate() {
		completed = (Timer.getFPGATimestamp() > endTime);

		if(!completed){
			Drivetrain.getInstance().setOpenLoopCmd(speed, 0);
		} else {
			Drivetrain.getInstance().setOpenLoopCmd(0, 0);
		}
	}

	@Override
	public void userForceStop() {
		Drivetrain.getInstance().setOpenLoopCmd(0, 0);
		return;
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
