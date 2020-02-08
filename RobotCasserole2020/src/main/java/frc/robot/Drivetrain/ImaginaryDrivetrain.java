
package frc.robot.Drivetrain;

import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;


public class ImaginaryDrivetrain extends Drivetrain{

    DrivetrainOpMode opModeCmd;
    DrivetrainOpMode opMode;
    DrivetrainOpMode prevOpMode;

    double DesRightRPM;
    double DesLeftRPM;
    double ActRightRPM;
    double ActLeftRPM;

    double desPoseAngle = 0;
    double actPoseAngle = 0;
    boolean headingAvailable = false;
    boolean useHeadingCommand = false;

    Signal ActualRightSimRPM;
    Signal ActualLeftSimRPM;
    Signal DesiredRightSimRPM;
    Signal DesiredLeftSimRPM;

    Signal DesiredPoseAngleDeg;
    Signal ActualPoseAngleDeg;

    final double DT_MAX_SPEED_FT_PER_SEC = 15.0;
    final double DT_MAX_ACCEL_FT_PER_SEC_PER_SEC = 8.0;


    public ImaginaryDrivetrain() {
        ActualRightSimRPM = new Signal("Drivetrain Sim Actual Right Speed", "RPM");
        ActualLeftSimRPM = new Signal("Drivetrain Sim Actual Left Speed", "RPM");
        DesiredRightSimRPM = new Signal("Drivetrain Sim Desired Right Speed", "RPM");
        DesiredLeftSimRPM = new Signal("Drivetrain Sim Desired Left Speed", "RPM");
        DesiredPoseAngleDeg = new Signal("Drivetrain Sim Desired Pose Angle", "deg");
        ActualPoseAngleDeg = new Signal("Drivetrain Sim Actual Pose Angle", "deg");
        dtPose = new RobotPose();
    }

    public void setOpenLoopCmd(double forwardReverseCmd, double rotationCmd) {
        opModeCmd = DrivetrainOpMode.kOpenLoop;
        useHeadingCommand = false;

        double motorSpeedLeftCMD = Utils.capMotorCmd(forwardReverseCmd - rotationCmd);
        double motorSpeedRightCMD = Utils.capMotorCmd(forwardReverseCmd + rotationCmd);

        DesLeftRPM = Utils.FT_PER_SEC_TO_RPM(DT_MAX_SPEED_FT_PER_SEC)*motorSpeedLeftCMD;
        DesRightRPM = Utils.FT_PER_SEC_TO_RPM(DT_MAX_SPEED_FT_PER_SEC)*motorSpeedRightCMD;
    }

   

    @Override
    public void update() {
        prevOpMode = opMode;
        opMode = opModeCmd;

        double angleErrCorrFactor  = 0;
        actPoseAngle = dtPose.getRobotPoseAngleDeg();

        if(useHeadingCommand){
            angleErrCorrFactor = 0.5*(actPoseAngle - desPoseAngle); //Simple P control to correct for angle errors
        } else {
            angleErrCorrFactor = 0;
        }

        if(opModeCmd == DrivetrainOpMode.kClosedLoopVelocity){
            //Asssume perfect drivetrain closed loop.
            ActLeftRPM = DesLeftRPM;
            ActRightRPM = DesRightRPM;
        } else if (opModeCmd == DrivetrainOpMode.kOpenLoop){
            ActLeftRPM = simMotor(ActLeftRPM, DesLeftRPM);
            ActRightRPM = simMotor(ActRightRPM, DesRightRPM);
        } 

        dtPose.setLeftMotorSpeed(ActLeftRPM + angleErrCorrFactor);
        dtPose.setRightMotorSpeed(ActRightRPM - angleErrCorrFactor);
        

        dtPose.update();

        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;

        ActualLeftSimRPM.addSample(sampleTimeMs, ActLeftRPM);
        ActualRightSimRPM.addSample(sampleTimeMs, ActRightRPM);
        DesiredLeftSimRPM.addSample(sampleTimeMs, DesLeftRPM);
        DesiredRightSimRPM.addSample(sampleTimeMs, DesRightRPM);
        ActualPoseAngleDeg.addSample(sampleTimeMs, actPoseAngle);
        DesiredPoseAngleDeg.addSample(sampleTimeMs, desPoseAngle);
    }

    

    private double simMotor(double actSpeedRPM, double desSpeedRPM){

        double accelFactor = Utils.FT_PER_SEC_TO_RPM( DT_MAX_ACCEL_FT_PER_SEC_PER_SEC * RobotConstants.MAIN_LOOP_Ts);
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
        useHeadingCommand = true;
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
        opModeCmd = DrivetrainOpMode.kClosedLoopVelocity;
        DesRightRPM = rightCmdRPM;
        DesLeftRPM = leftCmdRPM;
        useHeadingCommand = false;
    }

    @Override
    public void setInitialPose(double x_ft, double y_ft, double pose_angle_deg) {
        dtPose.resetToPosition(x_ft, y_ft, pose_angle_deg);
    }

    @Override
    public void setTurnToAngleCmd(double angle_cmd) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getTurnToAngleErrDeg() {
        // TODO Auto-generated method stub
        return 0;
    }

}