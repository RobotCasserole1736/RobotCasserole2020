package frc.robot.Autonomous.Modes;

import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventBallThiefA;
import frc.robot.Autonomous.Events.AutoEventBallThiefB;
import frc.robot.Autonomous.Events.AutoEventShootClose;
import frc.robot.Autonomous.Events.AutoEventSteakA;
import frc.robot.Autonomous.Events.AutoEventSteakB;
import frc.robot.Autonomous.Events.AutoEventTurn;
import frc.robot.Autonomous.Events.AutoEventTurnToVisionTarget;
import frc.robot.Drivetrain.Drivetrain;

public class Steak extends AutoMode {

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        Drivetrain.getInstance().setInitialPose(10, 11.5, 90);
        seq.addEvent(new AutoEventBallThiefA(4.0)); //Time is for intk, which is included
        seq.addEvent(new AutoEventBallThiefB(4.0)); //Time is for shoot prep, which is included
        seq.addEvent(new AutoEventTurn(6));
        seq.addEvent(new AutoEventTurnToVisionTarget(3.0));
        seq.addEvent(new AutoEventShootClose(3.0, 5));
        seq.addEvent(new AutoEventSteakA(4.0)); //Time is for intk, which is included
        seq.addEvent(new AutoEventTurn(25));
        seq.addEvent(new AutoEventSteakB(4.0));
        seq.addEvent(new AutoEventTurn(15));
        seq.addEvent(new AutoEventTurnToVisionTarget(5.0));
        seq.addEvent(new AutoEventShootClose(3.0, 5));
    }

    @Override
    public void setInitialPose(Drivetrain d) {
        d.setInitialPose(10, 11.5, 90);
    }
    
}

