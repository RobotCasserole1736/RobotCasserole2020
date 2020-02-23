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
public class AutoEventBallThiefB extends AutoEvent {
    PathPlannerAutoEvent driveBackward;

    //PrepToShoot Stuff
    double intkPrepDuration_s;
	double intkPrepEndTime;
    boolean intkPrepCompleted = true;

    //Waypoints always start at (0,0), and are referenced relative to the robot's
    // position and pose angle whenever the event starts running. Units must be inches.

    private final Waypoint[] waypoints_ft = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(-4.5, 1,  Pathfinder.d2r(-32))
    };

    public AutoEventBallThiefB(double intkPrepDuration_s_in) {
        driveBackward = new PathPlannerAutoEvent(waypoints_ft, true);
        intkPrepDuration_s = intkPrepDuration_s_in;
    }

    @Override
    public void userStart() {
        intkPrepEndTime = Timer.getFPGATimestamp() + intkPrepDuration_s;
        intkPrepCompleted = false;
        Supperstructure.getInstance().setPrepToShootDesired(true);
        Supperstructure.getInstance().setIntakeDesired(true);
        driveBackward.userStart();
    }

    @Override
    public void userUpdate() {
        intkPrepCompleted = (Timer.getFPGATimestamp() > intkPrepEndTime);
        if(intkPrepCompleted){
            Supperstructure.getInstance().setIntakeDesired(false);
            Supperstructure.getInstance().setPrepToShootDesired(false);
        }
        driveBackward.userUpdate();
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
        AutoEventBallThiefB autoEvent = new AutoEventBallThiefB(4.0); //time is for prep to shoot
		//TODO
		System.out.println("Done");
    }
}