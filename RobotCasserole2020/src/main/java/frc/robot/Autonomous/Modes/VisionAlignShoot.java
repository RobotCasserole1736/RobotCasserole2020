package frc.robot.Autonomous.Modes;

import edu.wpi.first.wpilibj.DriverStation;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventShootFar;
import frc.robot.Autonomous.Events.AutoEventTurnToVisionTarget;
import frc.robot.Drivetrain.Drivetrain;

public class VisionAlignShoot extends AutoMode {

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        seq.addEvent(new AutoEventTurnToVisionTarget(5.0));
        if(DriverStation.getInstance().isAutonomous()){
            seq.addEvent(new AutoEventShootFar(15.0,5));
        } else {
            seq.addEvent(new AutoEventShootFar(150.0,100));
        }
    }

    @Override
    public void setInitialPose(Drivetrain d) {
        // TODO Auto-generated method stub
        
    }
    
}

