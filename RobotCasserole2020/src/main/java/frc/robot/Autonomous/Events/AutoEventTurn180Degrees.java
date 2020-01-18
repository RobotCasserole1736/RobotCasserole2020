package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Drivetrain.CasseroleGyro;
import frc.robot.Drivetrain.Drivetrain;

public class AutoEventTurn180Degrees extends AutoEvent {
	
	private double targetAngle;
	private boolean weAreDone;
	private double currentTime = 0.0;
	private double startTime = 0.0;
    private double elapsedTime = 0.0;
	
	final double TURN_SPEED_RPM = 100;
	final double TIMEOUT_S = 5.0;
	
	@Override
	public void userStart() {
		// get gyro
		targetAngle = Drivetrain.getInstance().getGyroAngle() + 180;
		startTime = Timer.getFPGATimestamp();
	}

	@Override
	public void userUpdate() {
		//100 rpm to left
		//-110 rpm to right
		//is done = gyro read
		// gyro greater than target
		currentTime = Timer.getFPGATimestamp();
		elapsedTime = currentTime - startTime;
        //Drivetrain.getInstance().disableHeadingCmd();
        Drivetrain.getInstance().setClosedLoopSpeedCmd((-1*TURN_SPEED_RPM), (TURN_SPEED_RPM));
		if(Drivetrain.getInstance().getGyroAngle() > targetAngle || elapsedTime > TIMEOUT_S) {
			weAreDone = true;
			Drivetrain.getInstance().setClosedLoopSpeedCmd(0, 0);
		}else {
			weAreDone = false;
		}
	}

	@Override
	public void userForceStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isTriggered() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return weAreDone;
	}

}