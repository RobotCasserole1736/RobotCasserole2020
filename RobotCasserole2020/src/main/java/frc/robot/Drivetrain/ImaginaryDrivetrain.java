
package frc.robot.Drivetrain;

import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;


public class ImaginaryDrivetrain extends Drivetrain{

    DrivetrainOpMode opModeCmd;
    DrivetrainOpMode opMode;
    DrivetrainOpMode prevOpMode;

    double DesRightRPM;
    double DesLeftRPM;
    double ActRightRPM;
    double ActLeftRPM;

    double desPoseAngle = RobotPose.getInstance().INIT_POSE_T;
    double actPoseAngle = RobotPose.getInstance().INIT_POSE_T;
    boolean headingAvailable = false;

    Signal ActualRightSimRPM;
    Signal ActualLeftSimRPM;
    Signal DesiredRightSimRPM;
    Signal DesiredLeftSimRPM;

    final double DT_MAX_SPEED_FT_PER_SEC = 15.0;
    final double DT_MAX_ACCEL_FT_PER_SEC_PER_SEC = 8.0;


    public ImaginaryDrivetrain() {
        ActualRightSimRPM = new Signal("Drivetrain Sim Actual Right Speed", "RPM");
        ActualLeftSimRPM = new Signal("Drivetrain Sim Actual Left Speed", "RPM");
        DesiredRightSimRPM = new Signal("Drivetrain Sim Desired Right Speed", "RPM");
        DesiredLeftSimRPM = new Signal("Drivetrain Sim Desired Left Speed", "RPM");
    }

    public void setOpenLoopCmd(double forwardReverseCmd, double rotationCmd) {
        opModeCmd = DrivetrainOpMode.kOpenLoop;

        double motorSpeedLeftCMD = Utils.capMotorCmd(forwardReverseCmd + rotationCmd);
        double motorSpeedRightCMD = Utils.capMotorCmd(forwardReverseCmd - rotationCmd);

        DesLeftRPM = Utils.FT_PER_SEC_TO_RPM(DT_MAX_SPEED_FT_PER_SEC)*motorSpeedLeftCMD;
        DesRightRPM = Utils.FT_PER_SEC_TO_RPM(DT_MAX_SPEED_FT_PER_SEC)*motorSpeedRightCMD;
    }

   

    @Override
    public void update() {
        prevOpMode = opMode;
        opMode = opModeCmd;

        if(opModeCmd == DrivetrainOpMode.kClosedLoopVelocity){
            //Asssume perfect drivetrain closed loop.
            ActLeftRPM = DesLeftRPM;
            ActRightRPM = DesRightRPM;
            headingAvailable = false;
            actPoseAngle = desPoseAngle;

        } else if (opModeCmd == DrivetrainOpMode.kOpenLoop){
            ActLeftRPM = simMotor(ActLeftRPM, DesLeftRPM);
            ActRightRPM = simMotor(ActRightRPM, DesRightRPM);
            headingAvailable = false;
            actPoseAngle = RobotPose.getInstance().getRobotPoseAngleDeg();
        } 

        RobotPose.getInstance().update();

        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;

        ActualLeftSimRPM.addSample(sampleTimeMs, ActLeftRPM);
        ActualRightSimRPM.addSample(sampleTimeMs, ActRightRPM);
        DesiredLeftSimRPM.addSample(sampleTimeMs, DesLeftRPM);
        DesiredRightSimRPM.addSample(sampleTimeMs, DesRightRPM);
    }

    

    private double simMotor(double actSpeedRPM, double desSpeedRPM){

        double accelFactor = Utils.FT_PER_SEC_TO_RPM( DT_MAX_ACCEL_FT_PER_SEC_PER_SEC * 0.02);
        double maxSpd = Utils.FT_PER_SEC_TO_RPM(DT_MAX_SPEED_FT_PER_SEC);

        double delta = actSpeedRPM - desSpeedRPM ;

        if(Math.abs(desSpeedRPM) < 10){
            actSpeedRPM *= 0.90; // Static-ish Frictional constant
        } else {
            actSpeedRPM *= 0.98; // Frictional constant
        }


        if(delta < 0){
            //Accelerate
            actSpeedRPM += 1/accelFactor * Math.abs(delta);
        } else if (delta > 0){
            //Decellerate
            actSpeedRPM -= 1/accelFactor * Math.abs(delta);
        } else {
            //Cruse at constant speed
        }

        //Cap at absolute min/max
        if(actSpeedRPM > maxSpd){
            actSpeedRPM = maxSpd;
        } else if(actSpeedRPM < -1.0*maxSpd) {
            actSpeedRPM = -1.0*maxSpd;
        }

        return actSpeedRPM;


    }
    

    @Override
    public void setGyroLockCmd(double forwardReverseCmd) {
        //TODO - maybe
    }

    @Override
    public void setPositionCmd(double forwardReverseCmd, double angleError) {
        //TODO - maybe
    }

    @Override
    public boolean isGyroOnline() {
        return headingAvailable;
    }

    @Override
    public double getLeftWheelSpeedRPM() {
        return ActLeftRPM;
    }

    @Override
    public double getRightWheelSpeedRPM() {
        return ActRightRPM;
    }

    @Override
    public void updateGains(boolean force) {
        // No one here but us chickens

    }

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM, double headingCmdDeg) {
        opModeCmd = DrivetrainOpMode.kClosedLoopVelocity;
        DesRightRPM = rightCmdRPM;
        DesLeftRPM = leftCmdRPM;
        desPoseAngle = headingCmdDeg;
    }

    
    

    @Override
    public double getGyroAngle() {
        return actPoseAngle;
    }

    @Override
    public double getLeftNeo1Current() {
        //TODO
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

    @Override
    public void setClosedLoopSpeedCmd(double leftCmdRPM, double rightCmdRPM) {
        // TODO Auto-generated method stub

    }

}