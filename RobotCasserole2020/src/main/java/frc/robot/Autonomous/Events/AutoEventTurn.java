package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Drivetrain.Drivetrain;

public class AutoEventTurn extends AutoEvent {
	
	private double targetAngle;
	double desAngle;
	private boolean weAreDone;
	private double currentTime = 0.0;
	private double startTime = 0.0;
	private double elapsedTime = 0.0;
	private double errordeg=2.0;
	
	final double TURN_SPEED_RPM = 100;
	final double TIMEOUT_S = 5.0;

	double startT = 0;
	double startX = 0;
	double startY = 0;

	public AutoEventTurn(double inAngleDeg){
		desAngle=inAngleDeg;
	}
	
	@Override
	public void userStart() {
		// get gyro
		targetAngle = Drivetrain.getInstance().getGyroAngle() + desAngle;
		startTime = Timer.getFPGATimestamp();
		startT = Drivetrain.getInstance().dtPose.poseT;
		startX = Drivetrain.getInstance().dtPose.poseX;
		startY = Drivetrain.getInstance().dtPose.poseY;
		Drivetrain.getInstance().dtPose.setDesiredPose(startX, startY, startT + desAngle);
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
		
		if((elapsedTime > TIMEOUT_S) || (Math.abs(Drivetrain.getInstance().getGyroAngle()-targetAngle) < errordeg)){
			weAreDone = true;
			Drivetrain.getInstance().setClosedLoopSpeedCmd(0, 0);
		}else{
			weAreDone = false;
			if(Drivetrain.getInstance().getGyroAngle()-targetAngle<0){
				Drivetrain.getInstance().setClosedLoopSpeedCmd((-1*TURN_SPEED_RPM), (TURN_SPEED_RPM));
			}else{
				Drivetrain.getInstance().setClosedLoopSpeedCmd((TURN_SPEED_RPM), (-1*TURN_SPEED_RPM));
			}
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