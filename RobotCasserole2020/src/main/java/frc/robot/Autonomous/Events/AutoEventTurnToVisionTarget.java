package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Drivetrain.Drivetrain;
import frc.robot.VisionProc.CasseroleVision;
import frc.robot.VisionProc.VisionCamera;

public class AutoEventTurnToVisionTarget extends AutoEvent {
    
    private double startAngle;
    private double desAngle;
    private boolean weAreDone = false;
    private double currentTime = 0.0;
    private double startTime = 0.0;
    private double elapsedTime = 0.0;
    
    final double TURN_SPEED_RPM = 50;
    final double TIMEOUT_S = 5.0;
    final double ALLOWABLE_ANGLE_ERR_DEG = 2.0;

    private boolean stableTargetSeen = false;

    private VisionCamera cam;

    double startT = 0;
    double startX = 0;
    double startY = 0;

    public AutoEventTurnToVisionTarget(){
        cam = CasseroleVision.getInstance();
    }
    
    @Override
    public void userStart() {
        startTime = Timer.getFPGATimestamp();

        //Init the pose
        startT = Drivetrain.getInstance().dtPose.poseT;
        startX = Drivetrain.getInstance().dtPose.poseX;
        startY = Drivetrain.getInstance().dtPose.poseY;
        Drivetrain.getInstance().dtPose.setDesiredPose(startX, startY, startT);

        stableTargetSeen = false;
    }

    @Override
    public void userUpdate() {

        elapsedTime = Timer.getFPGATimestamp() - startTime;

        if(elapsedTime > TIMEOUT_S){
            weAreDone = true;
        } else {
            if(stableTargetSeen == false){
                Drivetrain.getInstance().setClosedLoopSpeedCmd(0, 0);
                if(cam.isTargetStable()){
                    desAngle = Drivetrain.getInstance().getGyroAngle() + cam.getTgtGeneralAngle(); //Calcuate what angle to turn toward
                    startTime = Timer.getFPGATimestamp(); //Restart timer
                    stableTargetSeen = true;
                } else {
                    //Wait for camera to report a stable target
                }
            } else {
                double angleErr = desAngle - Drivetrain.getInstance().getGyroAngle();

                //Bang-bang control of robot angle
                if(angleErr > 0){
                    //Angle error positive, turn toward the left to correct
                    Drivetrain.getInstance().setClosedLoopSpeedCmd((-1*TURN_SPEED_RPM), (TURN_SPEED_RPM));
                } else {
                    //Angle error negative, turn toward the right to correct
                    Drivetrain.getInstance().setClosedLoopSpeedCmd((TURN_SPEED_RPM), (-1*TURN_SPEED_RPM));
                }

                if(Math.abs(angleErr) < ALLOWABLE_ANGLE_ERR_DEG) {
                    weAreDone = true;
                }else {
                    weAreDone = false;
                }
            }
        }

        // When we finish, make sure the robot is 
        if(weAreDone){
            Drivetrain.getInstance().setClosedLoopSpeedCmd(0, 0);
        }


    }

    @Override
    public void userForceStop() {
        Drivetrain.getInstance().setClosedLoopSpeedCmd(0, 0);
        
    }

    @Override
    public boolean isTriggered() {
        return true;
    }

    @Override
    public boolean isDone() {
        return weAreDone;
    }

}