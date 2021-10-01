package frc.robot.Autonomous.Modes;

import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventDriveStraight;
import frc.robot.Autonomous.Events.AutoEventShootFar;
import frc.robot.Drivetrain.Drivetrain;

public class ShootOnly extends AutoMode {

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        seq.addEvent(new AutoEventDriveStraight(7.7));
        seq.addEvent(new AutoEventShootFar(15.0,5));
    }

    @Override
    public void setInitialPose(Drivetrain d) {
        d.setInitialPose(-8, 11.5, 90);
    }
    
}

