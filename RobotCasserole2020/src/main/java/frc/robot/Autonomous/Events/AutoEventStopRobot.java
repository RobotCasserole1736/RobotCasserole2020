package frc.robot.Autonomous.Events;

import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Drivetrain.Drivetrain;

/**
 * drive straight and stuff. Step response check (with typical smoothing)
 */
public class AutoEventStopRobot extends AutoEvent {

    boolean done = false;

    public AutoEventStopRobot() {
        
    }

    @Override
    public void userUpdate() {
        Drivetrain.getInstance().setClosedLoopSpeedCmd(0, 0);
        done = true;

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
        return true;
    }

    @Override
    public void userStart() {
        done = false;
    }

}