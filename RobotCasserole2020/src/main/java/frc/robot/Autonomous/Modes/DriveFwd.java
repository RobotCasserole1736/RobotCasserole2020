package frc.robot.Autonomous.Modes;

import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventDriveStraight;
import frc.robot.Drivetrain.Drivetrain;

public class DriveFwd extends AutoMode {

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        seq.addEvent(new AutoEventDriveStraight(1));
    }

    @Override
    public void setInitialPose(Drivetrain d) {
        // TODO Auto-generated method stub
        
    }
    
}

