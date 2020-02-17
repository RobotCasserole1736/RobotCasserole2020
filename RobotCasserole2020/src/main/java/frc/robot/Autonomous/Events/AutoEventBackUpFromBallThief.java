package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.PathPlannerAutoEvent;
import frc.robot.Supperstructure;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Waypoint;

/**
 * drive straight and stuff. Step response check (with typical smoothing)
 */
public class AutoEventBackUpFromBallThief extends AutoEvent {
    PathPlannerAutoEvent driveBackward;

    //PrepToShoot Stuff
    double prepSpeed;
	double prepDuration_s;
	double prepEndTime;
	boolean prepCompleted = true;

    //Waypoints always start at (0,0), and are referenced relative to the robot's
    // position and pose angle whenever the event starts running. Units must be inches.

    private final Waypoint[] waypoints_ft = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(-9.0, 1,  Pathfinder.d2r(-32))
    };

    public AutoEventBackUpFromBallThief(double prepDuration_s_in) {
        driveBackward = new PathPlannerAutoEvent(waypoints_ft, true);

        prepDuration_s = prepDuration_s_in;
    }

    @Override
    public void userStart() {
        driveBackward.userStart();

        prepEndTime = Timer.getFPGATimestamp() + prepDuration_s;
        prepCompleted = false;
		Supperstructure.getInstance().setPrepToShootDesired(true);
    }

    @Override
    public void userUpdate() {
        driveBackward.userUpdate();

        prepCompleted = (Timer.getFPGATimestamp() > prepEndTime);
		if (prepCompleted){
			Supperstructure.getInstance().setPrepToShootDesired(false);
		} else {
            Supperstructure.getInstance().setPrepToShootDesired(true);
            Supperstructure.getInstance().setIntakeDesired(false); //Because intake overrides prep to shoot
		}
    }

    @Override
    public void userForceStop() {
        driveBackward.userForceStop();

        Supperstructure.getInstance().setPrepToShootDesired(false);
    }

    @Override
    public boolean isTriggered() {
        return driveBackward.isTriggered();
    }

    @Override
    public boolean isDone() {
        return driveBackward.isDone();
    }
    
    public static void main(String[] args) {
		System.out.println("Starting path planner calculation...");
        AutoEventBackUpFromBallThief autoEvent = new AutoEventBackUpFromBallThief(4.0); //time is for prep to shoot
		//TODO
		System.out.println("Done");
    }
}