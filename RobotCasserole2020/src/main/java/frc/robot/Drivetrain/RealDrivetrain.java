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
    double angErr = 0;
    double headingCmdDeg = 0;
    //Hopefully these are the same 
    double lConversionFactor = leftEncoder.getVelocityConversionFactor();
    double rConversionFactor = rightEncoder.getVelocityConversionFactor();


    

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

    public void sampleSensors() {
        
        leftWheelSpeedRPM = dtLeftMaster.getEncoder().getVelocity() * lConversionFactor;
        rightWheelSpeedRPM = dtRightMaster.getEncoder().getVelocity() * rConversionFactor;

        dtNeoL1Current = dtLeftMaster.getOutputCurrent();
        dtNeoL2Current = dtLeftIntern.getOutputCurrent();
        dtNeoR1Current = dtRightMaster.getOutputCurrent();
        dtNeoR2Current = dtRightIntern.getOutputCurrent();

        
    }

    @Override
    public void update() {
        sampleSensors();
        




        
        
    }
    @Override
    public void setOpenLoopCmd(double forwardReverseCmd, double rotationCmd) {
        opModeCmd = DrivetrainOpMode.kOpenLoop;
        fwdRevCmd = forwardReverseCmd;
        rotCmd = rotationCmd;



    }

    @Override
    public void setGyroLockCmd(double forwardReverseCmd) {
        opModeCmd = DrivetrainOpMode.kGyroLock;
        fwdRevCmd = forwardReverseCmd;

    }

    @Override
    public void setPositionCmd(double forwardReverseCmd, double angleError) {
        opModeCmd = DrivetrainOpMode.kTargetPosition;
        fwdRevCmd = forwardReverseCmd;
        angErr = angleError;
        
    }

    @Override
    public boolean isGyroOnline() {
        // TODO We need gyro to do this
        return false;
    }

    @Override
    public double getLeftWheelSpeedRPM() {
        
        return leftWheelSpeedRPM;
    }

    @Override
    public double getRightWheelSpeedRPM() {
        
        return rightWheelSpeedRPM;
    }

    @Override
    public void updateGains(boolean force) {
        //TODO dont want to do this now

    }

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM) {
        opModeCmd = DrivetrainOpMode.kClosedLoopVelocity;
        leftWheelSpeedRPM  = leftCmdRPM;
        rightWheelSpeedRPM = rightCmdRPM;
    }

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM, double inHeadingCmdDeg) {
        opModeCmd = DrivetrainOpMode.kClosedLoopVelocity;
        leftWheelSpeedRPM  = leftCmdRPM;
        rightWheelSpeedRPM = rightCmdRPM;
        headingCmdDeg = inHeadingCmdDeg;


    }

    @Override
    public double getGyroAngle() {
        // TODO Need the gyro up and working
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