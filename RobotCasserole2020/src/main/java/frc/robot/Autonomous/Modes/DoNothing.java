package frc.robot.Autonomous.Modes;

import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Drivetrain.Drivetrain;

public class DoNothing extends AutoMode {

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        return; //nothing indeed
    }

    @Override
    public void setInitialPose(Drivetrain d) {
        // TODO Auto-generated method stub
        
    }
    
}

