package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Drivetrain.Drivetrain;
import frc.robot.Superstructure;

public class AutoEventIntake extends AutoEvent {
	
	double speed;
	double duration_s;
	double endTime;
	boolean completed = true;
	
	public AutoEventIntake(double duration_s_in) {
		duration_s = duration_s_in;
	}

	@Override
	public void userStart() {
		endTime = Timer.getFPGATimestamp() + duration_s;
        completed = false;
        Superstructure.getInstance().setIntakeDesired();
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
