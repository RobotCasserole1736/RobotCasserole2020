package frc.robot.Drivetrain;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANEncoder;
import frc.robot.RobotConstants;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import com.revrobotics.CANPIDController;
import frc.lib.Calibration.Calibration;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.IdleMode;

public class RealDrivetrain extends Drivetrain {

    
    double sampleTimeMS;
    
    CANSparkMax dtLeftMaster;
    CANEncoder leftEncoder;
    CANSparkMax dtLeftIntern;

    CANSparkMax dtRightMaster;
    CANEncoder rightEncoder;
    CANSparkMax dtRightIntern;
  

    CasseroleGyro dtGyro;
    

    
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
    
    //Closed loop wheel speed commands
    double leftWheelSpeedDesiredRPM = 0;
    double rightWheelSpeedDesiredRPM = 0;
    double leftWheelSpeedActualRPM = 0;
    double rightWheelSpeedActualRPM = 0;

    //Open loop wheel speed commands
    double dtLeftSpeedCmd = 0;
    double dtRightSpeedCmd = 0;

    double gyroAngle = 0;
    double headingCmdDeg = 0;
    boolean headingCmdAvailable = false;
    double headingCorrCmdRPM = 0;


    Signal leftWheelSpeedDesiredSig;
    Signal leftWheelSpeedActualSig;
    Signal rightWheelSpeedDesiredSig;
    Signal rightWheelSpeedActualSig;
    Signal headingCorrectionCmdSig;
    Signal rightMotorOutput;
    Signal leftMotorOutput;

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
    Calibration kPGyro;
    boolean calsUpdated;

    

    public RealDrivetrain(){
        dtPose = new RobotPose();

        dtLeftMaster = new CANSparkMax(RobotConstants.DT_LEFT_NEO_1_CANID, MotorType.kBrushless);
        dtLeftMaster.restoreFactoryDefaults();
        dtLeftIntern = new CANSparkMax(RobotConstants.DT_LEFT_NEO_2_CANID, MotorType.kBrushless);
        dtLeftIntern.restoreFactoryDefaults();
        dtRightMaster = new CANSparkMax(RobotConstants.DT_RIGHT_NEO_1_CANID, MotorType.kBrushless);
        dtRightMaster.restoreFactoryDefaults();
        dtRightIntern = new CANSparkMax(RobotConstants.DT_RIGHT_NEO_2_CANID, MotorType.kBrushless);
        dtRightIntern.restoreFactoryDefaults();

        dtLeftMaster.setInverted(true);
        dtLeftMaster.setIdleMode(IdleMode.kCoast);
        dtLeftIntern.setIdleMode(IdleMode.kCoast);
        dtRightMaster.setIdleMode(IdleMode.kCoast);
        dtRightMaster.setIdleMode(IdleMode.kCoast);


        dtLeftMaster.getEncoder().setVelocityConversionFactor(RobotConstants.DRIVETRAIN_GEAR_RATIO);
        dtRightMaster.getEncoder().setVelocityConversionFactor(RobotConstants.DRIVETRAIN_GEAR_RATIO);

        dtLPID = new CANPIDController(dtLeftMaster);
        dtRPID = new CANPIDController(dtRightMaster);
                
        leftWheelSpeedDesiredSig = new Signal("Drivetrain Left Wheel Desired Speed", "RPM");
        leftWheelSpeedActualSig = new Signal("Drivetrain Left Wheel Actual Speed", "RPM");
        rightWheelSpeedDesiredSig = new Signal("Drivetrain Right Wheel Desired Speed", "RPM");
        rightWheelSpeedActualSig = new Signal("Drivetrain Right Wheel Actual Speed", "RPM");
        headingCorrectionCmdSig = new Signal("Drivetrain Heading Correction Command", "RPM");
        rightMotorOutput = new Signal("Drivetrain Right Motor Output Duty Cycle", "pct");
        leftMotorOutput  = new Signal("Drivetrain Left Motor output Duty Cycle", "pct");

        currentL1Sig = new Signal("Left Master Moter Current", "Amps");
        currentL2Sig = new Signal("Left Intern Moter Current", "Amps");
        currentR1Sig = new Signal("Right Master Moter Current", "Amps");
        currentR2Sig = new Signal("Right Intern Moter Current", "Amps");

        kP = new Calibration("Drivetrain P Value", 0.00);
        kI = new Calibration("Drivetrain I Value", 0);
        kD = new Calibration("Drivetrain D Value", 0);
        kFF = new Calibration("Drivetrain F Value", 0);
        kPGyro = new Calibration("Drivetrain Gyro Comp P Value" , 0.0);

        
        dtLeftIntern.follow(dtLeftMaster);
        dtRightIntern.follow(dtRightMaster);

        dtGyro = new CasseroleGyro();
        dtGyro.calibrate();
        
        updateGains(true);

    }

    public void sampleSensors() {

        dtGyro.update();

        gyroAngle = dtGyro.getAngleDeg();
        
        leftWheelSpeedActualRPM = dtLeftMaster.getEncoder().getVelocity();
        rightWheelSpeedActualRPM = dtRightMaster.getEncoder().getVelocity();

        dtNeoL1Current = dtLeftMaster.getOutputCurrent();
        dtNeoL2Current = dtLeftIntern.getOutputCurrent();
        dtNeoR1Current = dtRightMaster.getOutputCurrent();
        dtNeoR2Current = dtRightIntern.getOutputCurrent();

        if(calsUpdated) {
            kP.acknowledgeValUpdate();
            kI.acknowledgeValUpdate();
            kD.acknowledgeValUpdate();
            kFF.acknowledgeValUpdate();
            calsUpdated = false;
        }
    }

    @Override
    public void update() {
        
        sampleSensors();
        opMode = opModeCmd;
    
        if(opMode == DrivetrainOpMode.kOpenLoop) {
            
            dtLeftSpeedCmd = Utils.capMotorCmd(fwdRevCmd - rotCmd);
            dtRightSpeedCmd = Utils.capMotorCmd(fwdRevCmd + rotCmd);

            dtLPID.setReference(dtLeftSpeedCmd*13, ControlType.kVoltage);
            dtRPID.setReference(dtRightSpeedCmd*13, ControlType.kVoltage);
           
        }        
        else if(opMode == DrivetrainOpMode.kClosedLoopVelocity) {
            if(headingCmdAvailable){
                headingCorrCmdRPM = kPGyro.get() * (headingCmdDeg - gyroAngle); //Positive headingCorrCmd means turn to the left, which increases the pose angle.
            } else {
                headingCorrCmdRPM = 0;
            }    

            leftWheelSpeedDesiredRPM  -= headingCorrCmdRPM;
            rightWheelSpeedDesiredRPM += headingCorrCmdRPM;

            dtLPID.setReference(leftWheelSpeedDesiredRPM, ControlType.kVelocity);
            dtRPID.setReference(rightWheelSpeedDesiredRPM, ControlType.kVelocity);

        }


        sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        leftWheelSpeedDesiredSig.addSample(sampleTimeMS, leftWheelSpeedDesiredRPM);
        leftWheelSpeedActualSig.addSample(sampleTimeMS, leftWheelSpeedActualRPM);
        rightWheelSpeedDesiredSig.addSample(sampleTimeMS, rightWheelSpeedDesiredRPM);
        rightWheelSpeedActualSig.addSample(sampleTimeMS, rightWheelSpeedActualRPM);
        headingCorrectionCmdSig.addSample(sampleTimeMS, headingCorrCmdRPM);
        rightMotorOutput.addSample(sampleTimeMS, dtRightMaster.getAppliedOutput());
        leftMotorOutput.addSample(sampleTimeMS, dtLeftMaster.getAppliedOutput());
        currentL1Sig.addSample(sampleTimeMS, dtNeoL1Current);
        currentL2Sig.addSample(sampleTimeMS, dtNeoL2Current);
        currentR1Sig.addSample(sampleTimeMS, dtNeoR1Current);
        currentR2Sig.addSample(sampleTimeMS, dtNeoR2Current);
        
        
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
    public boolean isGyroOnline() {
        return dtGyro.isOnline();
    }

    @Override
    public double getLeftWheelSpeedRPM() {
        return leftWheelSpeedActualRPM;
    }

    @Override
    public double getRightWheelSpeedRPM() {
        return rightWheelSpeedActualRPM;
    }

    @Override
    public void updateGains(boolean forceChange) {
        if(forceChange || haveCalsChanged()) {
            
            dtLPID.setP(kP.get());
            dtLPID.setI(kI.get());
            dtLPID.setD(kD.get());
            dtLPID.setFF(kFF.get());

            dtRPID.setP(kP.get());
            dtRPID.setI(kI.get());
            dtRPID.setD(kD.get());
            dtRPID.setFF(kFF.get());
            calsUpdated = true;
        }
    }

    private boolean haveCalsChanged() {
        return kP.isChanged() || kI.isChanged() || kD.isChanged() || kFF.isChanged();
    }
    

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM) {
        opModeCmd = DrivetrainOpMode.kClosedLoopVelocity;
        leftWheelSpeedDesiredRPM  = leftCmdRPM;
        rightWheelSpeedDesiredRPM = rightCmdRPM;
        headingCmdAvailable = false;
    }

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM, double inHeadingCmdDeg) {
        opModeCmd = DrivetrainOpMode.kClosedLoopVelocity;
        leftWheelSpeedDesiredRPM  = leftCmdRPM;
        rightWheelSpeedDesiredRPM = rightCmdRPM;
        headingCmdDeg = inHeadingCmdDeg;
        headingCmdAvailable = true;

    }

    @Override
    public double getGyroAngle() {
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


    @Override
    public void setInitialPose(double x_ft, double y_ft, double theta_deg) {
        //TODO - something with x_ft and y_ft?? Eeh.
        dtGyro.setCurAngle(theta_deg);
        gyroAngle = theta_deg;
    }
}