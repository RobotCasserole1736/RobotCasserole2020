package frc.robot.Autonomous.Events;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.PathWeaverJSONAutoEvent;
import frc.robot.Drivetrain.Drivetrain;

/**
 * drive straight and stuff. Step response check (with typical smoothing)
 */
public class AutoEventDriveSlalom extends AutoEvent {
    PathWeaverJSONAutoEvent pathPlanAutoEvent;

    // Waypoints always start at (0,0), and are referenced relative to the robot's
    // position and pose angle whenever the event starts running. Units must be
    // feet.

    // Positive rotation to the right, negative to the left
    // Positive X forward, Negative backward
    // Positive Y to the right, negative to the left.

    public AutoEventDriveSlalom() {

        pathPlanAutoEvent = new PathWeaverJSONAutoEvent("slalom_main.wpilib.json", 1.0);
        Drivetrain.getInstance().setClosedLoopRampRate(0.45);



    }

    @Override
    public void userStart() {
        pathPlanAutoEvent.userStart();
    }

    @Override
    public void userUpdate() {
        pathPlanAutoEvent.userUpdate();
    }

    @Override
    public void userForceStop() {
        pathPlanAutoEvent.userForceStop();
    }

    @Override
    public boolean isTriggered() {
        return pathPlanAutoEvent.isTriggered();
    }

    @Override
    public boolean isDone() {
        return pathPlanAutoEvent.isDone();
    }

    public static void main(String[] args) {
        System.out.println("Starting path planner calculation...");
        AutoEventDriveSlalom autoEvent = new AutoEventDriveSlalom();
		System.out.println("Done");
    }
}