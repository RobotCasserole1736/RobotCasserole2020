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
public class AutoEventTrenchToLoading extends AutoEvent {
    PathPlannerAutoEvent driveBackward;

    //PrepToShoot Stuff
	double intkPrepDuration_s;
	double intkPrepEndTime;
	boolean intkPrepCompleted = true;

    //Waypoints always start at (0,0), and are referenced relative to the robot's
    // position and pose angle whenever the event starts running. Units must be inches.

    private final Waypoint[] waypoints_ft = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(15.5,   5.6,  Pathfinder.d2r(7))
    };

    public AutoEventTrenchToLoading(double intkPrepDuration_s_in) {
        driveBackward = new PathPlannerAutoEvent(waypoints_ft, false,12,6);

        intkPrepDuration_s = intkPrepDuration_s_in;
    }

    @Override
    public void userStart() {
        driveBackward.userStart();

        intkPrepEndTime = Timer.getFPGATimestamp() + intkPrepDuration_s;
        intkPrepCompleted = false;
        Supperstructure.getInstance().setIntakeDesired(true);
		Supperstructure.getInstance().setPrepToShootDesired(true);
    }

    @Override
    public void userUpdate() {
        driveBackward.userUpdate();

        intkPrepCompleted = (Timer.getFPGATimestamp() > intkPrepEndTime);
		if (intkPrepCompleted){
            Supperstructure.getInstance().setPrepToShootDesired(false);
            Supperstructure.getInstance().setIntakeDesired(false);
        }
    }

    @Override
    public void userForceStop() {
        driveBackward.userForceStop();

        Supperstructure.getInstance().setPrepToShootDesired(false);
        Supperstructure.getInstance().setIntakeDesired(false);
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
        AutoEventTrenchToLoading autoEvent = new AutoEventTrenchToLoading(0.0); //time is for prep to shoot
		//TODO
		System.out.println("Done");
    }
}