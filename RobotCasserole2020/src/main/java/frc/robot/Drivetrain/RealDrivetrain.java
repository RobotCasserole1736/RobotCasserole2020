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
    double angleErr;
    double angleErrAccumulator;
    double turnToAngleCurSpeedLimit = 0;


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
    Signal headingCommandSig;
    Signal opModeSig;

    CANPIDController dtLPID;
    CANPIDController dtRPID;

    Calibration kP;
    Calibration kI;
    Calibration kD;
    Calibration kFF;
    Calibration kPGyro;

    Calibration turnToAnglekP;
    Calibration turnToAnglekI;
    Calibration turnToAngleMaxRPM;
    Calibration turnToAngleMaxRPMPerSec;

    Calibration AdenSensitivityCal;

    Calibration currentLimit;
    boolean calsUpdated;

    

    public RealDrivetrain(){
        dtPose = new RobotPose();

        dtLeftMaster = new CANSparkMax(RobotConstants.DT_LEFT_NEO_1_CAN_ID, MotorType.kBrushless);
        dtLeftMaster.restoreFactoryDefaults();
        dtLeftIntern = new CANSparkMax(RobotConstants.DT_LEFT_NEO_2_CAN_ID, MotorType.kBrushless);
        dtLeftIntern.restoreFactoryDefaults();
        dtRightMaster = new CANSparkMax(RobotConstants.DT_RIGHT_NEO_1_CAN_ID, MotorType.kBrushless);
        dtRightMaster.restoreFactoryDefaults();
        dtRightIntern = new CANSparkMax(RobotConstants.DT_RIGHT_NEO_2_CAN_ID, MotorType.kBrushless);
        dtRightIntern.restoreFactoryDefaults();

        dtLeftMaster.setInverted(true);
        dtLeftMaster.setIdleMode(IdleMode.kCoast);
        dtLeftIntern.setIdleMode(IdleMode.kCoast);
        dtRightMaster.setIdleMode(IdleMode.kCoast);
        dtRightIntern.setIdleMode(IdleMode.kCoast);


        dtLeftMaster.getEncoder().setVelocityConversionFactor(RobotConstants.DRIVETRAIN_GEAR_RATIO);
        dtRightMaster.getEncoder().setVelocityConversionFactor(RobotConstants.DRIVETRAIN_GEAR_RATIO);

        dtLPID = new CANPIDController(dtLeftMaster);
        dtRPID = new CANPIDController(dtRightMaster);
                
        leftWheelSpeedDesiredSig = new Signal("Drivetrain Left Wheel Desired Speed", "RPM");
        leftWheelSpeedActualSig = new Signal("Drivetrain Left Wheel Actual Speed", "RPM");
        rightWheelSpeedDesiredSig = new Signal("Drivetrain Right Wheel Desired Speed", "RPM");
        rightWheelSpeedActualSig = new Signal("Drivetrain Right Wheel Actual Speed", "RPM");
        headingCorrectionCmdSig = new Signal("Drivetrain Heading Correction Command", "RPM");
        headingCommandSig = new Signal("Drivetrain Desired Heading", "deg");
        rightMotorOutput = new Signal("Drivetrain Right Motor Output Duty Cycle", "pct");
        leftMotorOutput  = new Signal("Drivetrain Left Motor output Duty Cycle", "pct");
        opModeSig = new Signal("Drivetrain Op Mode", "mode");

        currentL1Sig = new Signal("Left Master Moter Current", "A");
        currentL2Sig = new Signal("Left Intern Moter Current", "A");
        currentR1Sig = new Signal("Right Master Moter Current", "A");
        currentR2Sig = new Signal("Right Intern Moter Current", "A");

        kP = new Calibration("Drivetrain P Value", 0.006);
        kI = new Calibration("Drivetrain I Value", 0);
        kD = new Calibration("Drivetrain D Value", 0.0096);
        kFF = new Calibration("Drivetrain F Value", 0.00195);
        kPGyro = new Calibration("Drivetrain Gyro Comp P Value" , 2.0);
        currentLimit = new Calibration("Drivetrain Per-Motor Smart Current Limit" , 80, 0, 200);
        turnToAnglekP= new Calibration("Drivetrain Turn To Angle kP", 3.0);
        turnToAnglekI= new Calibration("Drivetrain Turn To Angle kI", 0.1);
        turnToAngleMaxRPM= new Calibration("Drivetrain Turn To Angle Max RPM", 150, 0, 500);
        turnToAngleMaxRPMPerSec= new Calibration("Drivetrain Turn To Angle Max RPM sec", 75, 0, 5000);
        AdenSensitivityCal = new Calibration("Aden Sensitivity",1.2);

        dtLeftIntern.follow(dtLeftMaster);
        dtRightIntern.follow(dtRightMaster);

        dtLeftMaster.setOpenLoopRampRate(0.45);
        dtRightMaster.setOpenLoopRampRate(0.45);

        dtGyro = new CasseroleGyro();
        dtGyro.calibrate();
        
        updateGains(true);

        dtLeftMaster.burnFlash();
        dtLeftIntern.burnFlash();
        dtRightMaster.burnFlash();
        dtRightIntern.burnFlash();

    }

    @Override
    public void calGyro(){
        dtGyro.calibrate();
    }

    @Override
    public void setMotorMode(IdleMode inMode){
        dtLeftMaster.setIdleMode(inMode);
        dtLeftIntern.setIdleMode(inMode);
        dtRightMaster.setIdleMode(inMode);
        dtRightIntern.setIdleMode(inMode);
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

        prevOpMode = opMode;
        opMode = opModeCmd;
        
        sampleSensors();

        if(prevOpMode != DrivetrainOpMode.kTurnToAngle && opMode == DrivetrainOpMode.kTurnToAngle){
            //Reset for start of turn-to-angle
            angleErrAccumulator = 0;
            turnToAngleCurSpeedLimit = 0;
        }
    
        if(opMode == DrivetrainOpMode.kOpenLoop) {
            
            dtLeftSpeedCmd = Utils.capMotorCmd(fwdRevCmd - rotCmd);
            dtRightSpeedCmd = Utils.capMotorCmd(fwdRevCmd + rotCmd);

            dtLPID.setReference(dtLeftSpeedCmd*13, ControlType.kVoltage);
            dtRPID.setReference(dtRightSpeedCmd*13, ControlType.kVoltage);
           
        }        
        else if(opMode == DrivetrainOpMode.kClosedLoopVelocity) {
            if(headingCmdAvailable){
                angleErr = headingCmdDeg - gyroAngle;
                headingCorrCmdRPM = kPGyro.get() * (angleErr); //Positive headingCorrCmd means turn to the left, which increases the pose angle.
            } else {
                angleErr = 0;
                headingCorrCmdRPM = 0;
            }    

            leftWheelSpeedDesiredRPM  -= headingCorrCmdRPM;
            rightWheelSpeedDesiredRPM += headingCorrCmdRPM;

            dtLPID.setReference(leftWheelSpeedDesiredRPM, ControlType.kVelocity);
            dtRPID.setReference(rightWheelSpeedDesiredRPM, ControlType.kVelocity);

        } else if (opMode == DrivetrainOpMode.kTurnToAngle){

            angleErr = (headingCmdDeg - gyroAngle);

            if(Math.abs(angleErr) < 8.0){
                //Only accumulate when we're close to the target.
                angleErrAccumulator += angleErr;
            } else {
                angleErrAccumulator = 0; //otherwise let P take over.
            }

            if(turnToAngleCurSpeedLimit < turnToAngleMaxRPM.get()){
                turnToAngleCurSpeedLimit += turnToAngleMaxRPMPerSec.get() * RobotConstants.MAIN_LOOP_Ts;
            }

            //Rate-limited KI control of robot angle
            headingCorrCmdRPM = turnToAnglekP.get() * angleErr + turnToAnglekI.get() * angleErrAccumulator;

            if(headingCorrCmdRPM > turnToAngleCurSpeedLimit){
                headingCorrCmdRPM = turnToAngleCurSpeedLimit;
            } else if (headingCorrCmdRPM < -1.0*turnToAngleCurSpeedLimit){
                headingCorrCmdRPM = -1.0 * turnToAngleCurSpeedLimit;
            }

            leftWheelSpeedDesiredRPM   = -1*headingCorrCmdRPM;
            rightWheelSpeedDesiredRPM  = headingCorrCmdRPM;

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
        headingCommandSig.addSample(sampleTimeMS, headingCmdDeg);
        opModeSig.addSample(sampleTimeMS, opMode.toInt());
        
        
    }
    
    @Override
    public void setOpenLoopCmd(double forwardReverseCmd, double rotationCmd) {
        opModeCmd = DrivetrainOpMode.kOpenLoop;
        fwdRevCmd = forwardReverseCmd;
        if(rotationCmd<0){
            rotCmd = Math.pow(Math.abs(rotationCmd),2.7)*-1;
        }else{
            rotCmd = Math.pow(Math.abs(rotationCmd),2.7);
        }
        //Aden likes cheesy drive, so we do this thing.
        //No he doesn't so we don't do this thing.
        // if(Math.abs(fwdRevCmd) < 0.1){
        //     //coutner rotate allowed
        //     fwdRevCmd = 0;
        //     rotCmd = rotationCmd;
        // } else {
        //     fwdRevCmd = forwardReverseCmd;
        //     rotCmd = Math.abs(forwardReverseCmd) * rotationCmd * AdenSensitivityCal.get(); //Modifiy scalar for sensitivity
        // }
    }

    @Override
    public void setGyroLockCmd(double forwardReverseCmd) {
        opModeCmd = DrivetrainOpMode.kGyroLock;
        fwdRevCmd = forwardReverseCmd;
    }

    @Override
    public void setTurnToAngleCmd(double angle_des) {
        opModeCmd = DrivetrainOpMode.kTurnToAngle;
        headingCmdDeg = angle_des;
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

            dtLeftMaster.setSmartCurrentLimit((int)currentLimit.get());
            dtLeftIntern.setSmartCurrentLimit((int)currentLimit.get());
            dtRightMaster.setSmartCurrentLimit((int)currentLimit.get());
            dtRightIntern.setSmartCurrentLimit((int)currentLimit.get());

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

    @Override
    public double getTurnToAngleErrDeg() {
        return angleErr;
    }
}