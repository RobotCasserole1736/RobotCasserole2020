package frc.robot.Drivetrain;

import edu.wpi.first.wpilibj.RobotBase;

public abstract class Drivetrain {

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

    public abstract void setMotorCommandOpenLoop(double pctVbatLeft, double pctVbatRight);

    public abstract void setClosedLoopSpeedCmd(double leftVel_ftpsec, double rightVel_ftpsec);

}