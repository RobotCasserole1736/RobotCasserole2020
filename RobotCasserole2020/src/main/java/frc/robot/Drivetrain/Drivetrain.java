package frc.robot.Drivetrain;

import frc.robot.RobotSimMode;

public abstract class Drivetrain {
    
    //Also added this so that PathPlannerAutoEvent would be happy
    public static final double WHEEL_ROLLING_RADIUS_FT = 0.24; //Radius of 6in wheel

    /* Singleton infrastructure */
    private static Drivetrain instance;

    public static Drivetrain getInstance() {
        if (instance == null) {
            //On init, choose whether we want a real or fake drivetrain
            if(RobotSimMode.getInstance().runSimulation()){
                instance = new ImaginaryDrivetrain(); 
            } else {
                instance = new RealDrivetrain(); 
            }
        }
        return instance;
    }

    public RobotPose dtPose;

    public abstract void update();
    public abstract void setOpenLoopCmd(double forwardReverseCmd, double rotaionCmd);
    public abstract void setGyroLockCmd(double forwardReverseCmd);
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
    
    public double WHEEL_RPM_TO_FPS(double rpm_in){
        return rpm_in / 60 * 2 * Math.PI * WHEEL_ROLLING_RADIUS_FT;
    }

    public double getRobotSpeedfps(){
        return Math.abs( ( WHEEL_RPM_TO_FPS(getRightWheelSpeedRPM()) + WHEEL_RPM_TO_FPS(getLeftWheelSpeedRPM()) )  / 2  );
    }

}