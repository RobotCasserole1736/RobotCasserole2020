package frc.robot.Autonomous.Events;

import edu.wpi.first.wpilibj.Timer;
import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.PathPlannerAutoEvent;
import frc.robot.Supperstructure;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Waypoint;

/**
 * go to scale on left.
 */
public class AutoEventSideSteakC extends AutoEvent {
    PathPlannerAutoEvent driveForward;

    //PrepToShoot Stuff
    double prepSpeed;
	double prepDuration_s;
	double prepEndTime;
	boolean prepCompleted = true;

    private final Waypoint[] waypoints_ft = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(7.5, 1, Pathfinder.d2r(-29))
    };

    public AutoEventSideSteakC(double prepDuration_s_in) {
        prepDuration_s = prepDuration_s_in;
        driveForward = new PathPlannerAutoEvent(waypoints_ft, false,12,6);
    }

    @Override
    public void userStart() {
        prepEndTime = Timer.getFPGATimestamp() + prepDuration_s;
        prepCompleted = false;
		Supperstructure.getInstance().setPrepToShootDesired(true);
        driveForward.userStart();
    }

    @Override
    public void userUpdate() {
        prepCompleted = (Timer.getFPGATimestamp() > prepEndTime);
		if (prepCompleted){
			Supperstructure.getInstance().setPrepToShootDesired(false);
		} else {
            Supperstructure.getInstance().setPrepToShootDesired(true);
            Supperstructure.getInstance().setIntakeDesired(false); //Because intake overrides prep to shoot
		}
        driveForward.userUpdate();
    }
    
    @Override
    public void userForceStop() {
        Supperstructure.getInstance().setPrepToShootDesired(false);
        driveForward.userForceStop();
    }

    @Override
    public boolean isTriggered() {
        return driveForward.isTriggered();
    }

    @Override
    public boolean isDone() {
        return driveForward.isDone();
    }

    public static void main(String[] args) {
        AutoEventSideSteakC autoEvent = new AutoEventSideSteakC(4.0); //time is for intk
		//TODO
		System.out.println("Done");
    }
}