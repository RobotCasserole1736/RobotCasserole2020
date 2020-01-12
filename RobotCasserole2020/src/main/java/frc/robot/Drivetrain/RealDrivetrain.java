package frc.robot.Drivetrain;

import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANEncoder;
import edu.wpi.first.hal.sim.mockdata.PDPDataJNI;
import edu.wpi.first.wpilibj.CAN;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import frc.robot.RobotConstants;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import com.revrobotics.CANPIDController;
import frc.lib.Calibration.Calibration;

public class RealDrivetrain extends Drivetrain {

    
    double sampleTimeMS;


    
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

    Signal leftWheelSpeedDesiredSig;
    Signal leftWheelSpeedActualSig;
    Signal rightWheelSpeedDesiredSig;
    Signal rightWheelSpeedActualSig;

    Signal currentL1Sig;
    Signal currentL2Sig;
    Signal currentR1Sig;
    Signal currentR2Sig;

    CANPIDController dtLPID;
    CANPIDController dtRPID;

    Calibration kP;
    Calibration kI;
    Calibration kD;
    Calibration kFF;
    double kIz;
    boolean calsUpdated;

    

    public RealDrivetrain(){
        dtLeftIntern = new CANSparkMax(RobotConstants.DT_LEFT_NEO_1_CANID, MotorType.kBrushless);
        dtLeftIntern = new CANSparkMax(RobotConstants.DT_LEFT_NEO_2_CANID, MotorType.kBrushless);
        dtLeftIntern = new CANSparkMax(RobotConstants.DT_RIGHT_NEO_2_CANID, MotorType.kBrushless);
        dtLeftIntern = new CANSparkMax(RobotConstants.DT_RIGHT_NEO_2_CANID, MotorType.kBrushless);

        dtLPID = new CANPIDController(dtLeftMaster);
        dtRPID = new CANPIDController(dtRightMaster);
                
        leftWheelSpeedDesiredSig = new Signal("Drivetrain Left Wheel Desired Speed", "RPM");
        leftWheelSpeedActualSig = new Signal("Drivetrain Left Wheel Actual Speed", "RPM");
        rightWheelSpeedDesiredSig = new Signal("Drivetrain Right Wheel Desired Speed", "RPM");
        rightWheelSpeedActualSig = new Signal("Drivetrain Right Wheel Actual Speed", "RPM");

        currentL1Sig = new Signal("Left Master Moter Current", "Amps");
        currentL2Sig = new Signal("Left Intern Moter Current", "Amps");
        currentR1Sig = new Signal("Right Master Moter Current", "Amps");
        currentR2Sig = new Signal("Right Intern Moter Current", "Amps");

        kP = new Calibration("Drivetrain P Value", 0);
        kI = new Calibration("Drivetrain I Value", 0);
        kD = new Calibration("Drivetrain D Value", 0);
        kFF = new Calibration("Drivetrain F Value", 0);
        kIz = 0;

        
            dtLeftIntern.follow(dtLeftMaster);
            dtRightIntern.follow(dtRightMaster);
        

    }

    public void sampleSensors() {
        
        leftWheelSpeedRPM = dtLeftMaster.getEncoder().getVelocity() * lConversionFactor;
        rightWheelSpeedRPM = dtRightMaster.getEncoder().getVelocity() * rConversionFactor;

        dtNeoL1Current = dtLeftMaster.getOutputCurrent();
        dtNeoL2Current = dtLeftIntern.getOutputCurrent();
        dtNeoR1Current = dtRightMaster.getOutputCurrent();
        dtNeoR2Current = dtRightIntern.getOutputCurrent();

        leftWheelSpeedDesiredSig.addSample(sampleTimeMS, fwdRevCmd);
        leftWheelSpeedActualSig.addSample(sampleTimeMS, leftWheelSpeedRPM);
        rightWheelSpeedDesiredSig.addSample(sampleTimeMS, fwdRevCmd);
        rightWheelSpeedActualSig.addSample(sampleTimeMS, rightWheelSpeedRPM);

        kP.acknowledgeValUpdate();
        kI.acknowledgeValUpdate();
        kD.acknowledgeValUpdate();
        kFF.acknowledgeValUpdate();

        currentL1Sig.addSample(sampleTimeMS, dtNeoL1Current);
        currentL2Sig.addSample(sampleTimeMS, dtNeoL2Current);
        currentR1Sig.addSample(sampleTimeMS, dtNeoR1Current);
        currentR2Sig.addSample(sampleTimeMS, dtNeoR2Current);
        
        
    }

    @Override
    public void update() {
        sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        sampleSensors();

        if(opMode == DrivetrainOpMode.kOpenLoop) {
        
            //dtLeftMaster.setVoltage(fwdRevCmd);
            //dtRightMaster.setVoltage(fwdRevCmd);
            //What does this do 
            //dtPID.setReference(0, ControlType.kVoltage);
            
        }        
        else if(opMode == DrivetrainOpMode.kClosedLoopVelocity) {
            //dtLeftMaster.setVoltage(dtLPID.);
        }




        
        
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
    public void updateGains(boolean forceChange) {
        if(forceChange || haveCalsChanged()) {
            //dtPID.setP(kP.get());
            //dtPID.setI(kI.get());
            //dtPID.setD(kD.get());
            //dtPID.setIZone(kIz);
            //dtPID.setFF(kFF.get());
            //calsUpdated = true;
        }


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
        
        return dtNeoL2Current;
    }

    @Override
    public double getRightNeo1Current() {
       
        return dtNeoR1Current;
    }

    @Override
    public double getRightNeo2Current() {
      
        return dtNeoR2Current;
    }
    public double getGyroLockRotationCmd(){
        return gyroLockRotationCmd;
    }
    private boolean haveCalsChanged() {
        return kP.isChanged() || kI.isChanged() || kD.isChanged() || kFF.isChanged();
    }
}