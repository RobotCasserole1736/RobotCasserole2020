package frc.robot.Drivetrain;

import edu.wpi.first.wpilibj.RobotBase;

public abstract class Drivetrain {
    
	//Debug only //Copied this from the 2018 code so that PathPlannerAutoEvent would be happy
	public double leftAutoCmdFtPerSec = 0;
	public double rightAutoCmdFtPerSec = 0;
    public double autoTimestamp = 0;
    
    //Also added this so that PathPlannerAutoEvent would be happy
    public static final double WHEEL_ROLLING_RADIUS_FT = 0.24; //Radius of 6in wheel

    /* Singleton infrastructure */
    private static Drivetrain instance;

    public static Drivetrain getInstance() {
        if (instance == null) {
            //On init, choose whether we want a real or fake drivetrain
            if(RobotBase.isReal()){
                instance = new RealDrivetrain(); 
            } else {
                instance = new ImaginaryDrivetrain();
            }
        }
        return instance;
    }

    public abstract void update();
    public abstract void setOpenLoopCmd(double forwardReverseCmd, double rotaionCmd);
    public abstract void setGyroLockCmd(double forwardReverseCmd);
    public abstract void setPositionCmd(double forwardReverseCmd, double angleError);
    public abstract boolean isGyroOnline();
    public abstract double getLeftWheelSpeedRPM();
    public abstract double getRightWheelSpeedRPM();
    public abstract void updateGains(boolean force);
    public abstract void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM);
    public abstract void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM, double headingCmdDeg);
    public abstract double getGyroAngle();
    public abstract double getLeftNeo1Current();
    public abstract double getLeftNeo2Current();
    public abstract double getRightNeo1Current();
    public abstract double getRightNeo2Current();
    public abstract void setInitialPose(double x_ft, double y_ft, double theta_ft);

}