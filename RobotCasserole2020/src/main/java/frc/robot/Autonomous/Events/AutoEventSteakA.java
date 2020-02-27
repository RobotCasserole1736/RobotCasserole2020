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
public class AutoEventSteakA extends AutoEvent {
    PathPlannerAutoEvent driveForward;
    //stuff for Intake
	double prepDuration_s;
	double prepEndTime;
	boolean prepCompleted = true;

    private final Waypoint[] waypoints_ft_pt1 = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(-17.5,   -9,  Pathfinder.d2r(0)),
        
    };


    public AutoEventSteakA(double prepDuration_s_in) {
        prepDuration_s = prepDuration_s_in;
        driveForward=new PathPlannerAutoEvent(waypoints_ft_pt1, true,12,6);

        
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
			Supperstructure.getInstance().setIntakeDesired(false);
        }
        driveForward.userUpdate();
    }
    
    @Override
    public void userForceStop() {
        Supperstructure.getInstance().setIntakeDesired(false);
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
        AutoEventSteakA autoEvent = new AutoEventSteakA(4.0); //time is for intk
		//TODO
		System.out.println("Done");
    }
}