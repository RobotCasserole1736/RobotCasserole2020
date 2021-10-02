package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.robot.Drivetrain.Drivetrain;

/**
 * drive straight and stuff. Step response check (with typical smoothing)
 */
public class AutoEventDrivetrainTakeOutSlack extends AutoEvent {

    double startTime = 0;
    boolean reversed = false;
    final double DURATION_S = 0.35;

    public AutoEventDrivetrainTakeOutSlack(boolean reversed) {
        this.reversed = reversed;
    }

    @Override
    public void userUpdate() {
        Drivetrain.getInstance().setOpenLoopCmd( 0.07 * (reversed?-1.0:1.0), 0);

    }

    @Override
    public void userForceStop() {
         Drivetrain.getInstance().setOpenLoopCmd(0, 0);
    }

    @Override
    public boolean isTriggered() {
        return true;
    }

    @Override
    public boolean isDone() {
        return (Timer.getFPGATimestamp() - startTime)  > DURATION_S;
    }

    @Override
    public void userStart() {
        startTime = Timer.getFPGATimestamp();
    }

}