package frc.robot.Autonomous.Events;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.PathPlannerAutoEvent;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Waypoint;

/**
 * drive straight and stuff. Step response check (with typical smoothing)
 */
public class AutoEventDriveStraight extends AutoEvent {
    PathPlannerAutoEvent driveBackward;

    //Waypoints always start at (0,0), and are referenced relative to the robot's
    // position and pose angle whenever the event starts running. Units must be inches.


    public AutoEventDriveStraight(double inDis){
        final Waypoint[] waypoints_ft = new Waypoint[] {
            new Waypoint(0,      0,  Pathfinder.d2r(0)),
            new Waypoint(inDis, 0,  Pathfinder.d2r(0))
        };
        if(inDis<0){
            driveBackward = new PathPlannerAutoEvent(waypoints_ft, true,6,6);
        }else{
            driveBackward = new PathPlannerAutoEvent(waypoints_ft, false,6,6);
        }
        
    }

    @Override
    public void userStart() {
        driveBackward.userStart();
    }

    @Override
    public void userUpdate() {
        driveBackward.userUpdate();
    }

    @Override
    public void userForceStop() {
        driveBackward.userForceStop();
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
        AutoEventDriveStraight autoEvent = new AutoEventDriveStraight(0); 
		//TODO
		System.out.println("Done");
    }
}