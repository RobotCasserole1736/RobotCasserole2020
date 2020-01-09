package frc.robot.Drivetrain;

import com.revrobotics.CANSparkMax;

public class RealDrivetrain extends Drivetrain {

    CANSparkMax DtLeftMaster;
    CANSparkMax DtRightMaster;
    
    CANSparkMax DtLeftIntern;
    CANSparkMax DtRightIntern;

    //State Data
    

    double fwdRevCmd = 0;
    double rotCmd = 0;

    public RealDrivetrain(){
    
        DrivetrainOpMode opMode; /* The present operational mode */
        DrivetrainOpMode opModeCmd; /* The most recently commanded operational mode */
        DrivetrainOpMode prevOpMode; /* the previous operational mode */
        
        for(int i =0; i < 10; i++){
            DtLeftIntern.follow(DtLeftMaster);
            DtRightIntern.follow(DtRightMaster);
        }
        

    }

    @Override
    public void update() {
        // TODO Auto-generated method stub
        DtLeftIntern.set(fwdRevCmd);
        
    }

    @Override
    public void setOpenLoopCmd(double forwardReverseCmd, double rotaionCmd) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setGyroLockCmd(double forwardReverseCmd) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPositionCmd(double forwardReverseCmd, double angleError) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isGyroOnline() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public double getLeftWheelSpeedRPM() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getRightWheelSpeedRPM() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void updateGains(boolean force) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM, double headingCmdDeg) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getGyroAngle() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getLeftNeo1Current() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getLeftNeo2Current() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getRightNeo1Current() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getRightNeo2Current() {
        // TODO Auto-generated method stub
        return 0;
    }

}