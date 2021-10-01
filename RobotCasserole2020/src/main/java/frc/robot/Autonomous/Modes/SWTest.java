package frc.robot.Autonomous.Modes;

import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.robot.Autonomous.Events.AutoEventShootClose;
import frc.robot.Autonomous.Events.AutoEventTurnToVisionTarget;
import frc.robot.Drivetrain.Drivetrain;

public class SWTest extends AutoMode {

    @Override
    public void addStepsToSequencer(AutoSequencer seq) {
        // seq.addEvent(new AutoEventPathPlanTest());
        // seq.addEvent(new AutoEventReversePathPlanTest());
        // seq.addEvent(new AutoEventStopRobot());

        //seq.addEvent(new AutoEventTurn(90));
        //seq.addEvent(new AutoEventBackUpThreeFeet());

        seq.addEvent(new AutoEventTurnToVisionTarget(5.0));
        seq.addEvent(new AutoEventShootClose(15.0, 5));    
    }

    @Override
    public void setInitialPose(Drivetrain d) {
        // TODO Auto-generated method stub
        
    }
    
}

