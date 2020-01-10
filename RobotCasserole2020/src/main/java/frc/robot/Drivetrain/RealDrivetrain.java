package frc.robot.Drivetrain;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANEncoder;
import edu.wpi.first.hal.sim.mockdata.PDPDataJNI;
import edu.wpi.first.wpilibj.CAN;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import frc.robot.RobotConstants;

public class RealDrivetrain extends Drivetrain {

    //TODO Put in Robot Constants


    
    CANSparkMax dtLeftMaster;
    CANSparkMax dtRightMaster;
    CANEncoder leftEncoder;

    CANSparkMax dtLeftIntern;
    CANSparkMax dtRightIntern;
    CANEncoder rightEncoder;

    Gyro dtGyro;
    

    
    //State Data
    double fwdRevCmd = 0;
    double rotCmd = 0;

    DrivetrainOpMode opMode; /* The present operational mode */
    DrivetrainOpMode opModeCmd; /* The most recently commanded operational mode */
    DrivetrainOpMode prevOpMode; /* the previous operational mode */
    
    //Sensor Data
    
    double dtNeoL1Current = 0;
    double dtNeoL2Current = 0;
    double dtNeoR1Current = 0;
    double dtNeoR2Current = 0;
    double leftWheelSpeedRPM = 0;
    double rightWheelSpeedRPM = 0;
    double gyroAngle = 0;
    double gyroLockRotationCmd = 0;


    

    public RealDrivetrain(){
        dtLeftIntern = new CANSparkMax(RobotConstants.DT_LEFT_NEO_1_CANID, MotorType.kBrushless);
        dtLeftIntern = new CANSparkMax(RobotConstants.DT_LEFT_NEO_2_CANID, MotorType.kBrushless);
        dtLeftIntern = new CANSparkMax(RobotConstants.DT_RIGHT_NEO_2_CANID, MotorType.kBrushless);
        dtLeftIntern = new CANSparkMax(RobotConstants.DT_RIGHT_NEO_2_CANID, MotorType.kBrushless);
        


        for(int i =0; i < 10; i++){
            dtLeftIntern.follow(dtLeftMaster);
            dtRightIntern.follow(dtRightMaster);
        }
        

    }

   
    @Override
    public void update() {
        // TODO Auto-generated method stub
        //sensors
        dtNeoL1Current = dtLeftMaster.getOutputCurrent();
        dtNeoL2Current = dtLeftIntern.getOutputCurrent();
        dtNeoR1Current = dtRightMaster.getOutputCurrent();
        dtNeoR2Current = dtRightIntern.getOutputCurrent();

        leftWheelSpeedRPM = dtLeftMaster.getEncoder().getVelocity();
        rightWheelSpeedRPM = dtRightMaster.getEncoder().getVelocity();



        
        
    }
    @Override
    public void setOpenLoopCmd(double forwardReverseCmd, double rotaionCmd) {
        opModeCmd = DrivetrainOpMode.kOpenLoop;
        forwardReverseCmd = fwdRevCmd;

    }

    @Override
    public void setGyroLockCmd(double forwardReverseCmd) {
        opModeCmd = DrivetrainOpMode.kGyroLock;
        forwardReverseCmd = fwdRevCmd;

    }

    @Override
    public void setPositionCmd(double forwardReverseCmd, double angleError) {
        opModeCmd = DrivetrainOpMode.kTargetPosition;

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
        return gyroAngle;
    }

    @Override
    public double getLeftNeo1Current() {
        
        return dtNeoL1Current;
    }

    @Override
    public double getLeftNeo2Current() {
        // TODO Auto-generated method stub
        return dtNeoL2Current;
    }

    @Override
    public double getRightNeo1Current() {
        // TODO Auto-generated method stub
        return dtNeoR1Current;
    }

    @Override
    public double getRightNeo2Current() {
        // TODO Auto-generated method stub
        return dtNeoR2Current;
    }
    public double getGyroLockRotationCmd(){
        return gyroLockRotationCmd;
    }
}