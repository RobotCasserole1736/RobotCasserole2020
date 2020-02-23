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
public class AutoEventNoStealSteakB extends AutoEvent {
    PathPlannerAutoEvent[] driveForward;

    int idx;
    int len;
    //stuff for Intake
	double intkPrepDuration_s;
	double intkPrepEndTime;
	boolean intkPrepCompleted = true;

    


    private final Waypoint[] citrus_waypoints_ft_pt0 = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(-4.6,      -5.6,  Pathfinder.d2r(0)),
    };

    private final Waypoint[] citrus_waypoints_ft_pt1 = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(12.6,   -2,  Pathfinder.d2r(-10)),
    };


    public AutoEventNoStealSteakB(double intkPrepDuration_s_in) {
        intkPrepDuration_s = intkPrepDuration_s_in;
        idx=0;
        driveForward=new PathPlannerAutoEvent[2];
        driveForward[0] = new PathPlannerAutoEvent(citrus_waypoints_ft_pt0, true,12,6);
        driveForward[1] = new PathPlannerAutoEvent(citrus_waypoints_ft_pt1, false,8,6);
        len=1;
    
        
    }

    @Override
    public void userStart() {
        intkPrepEndTime = Timer.getFPGATimestamp() + intkPrepDuration_s;
        intkPrepCompleted = false;
        Supperstructure.getInstance().setIntakeDesired(true);
        Supperstructure.getInstance().setPrepToShootDesired(true);

        driveForward[idx].userStart();
    }

    @Override
    public void userUpdate() {
        intkPrepCompleted = (Timer.getFPGATimestamp() > intkPrepEndTime);
		if (intkPrepCompleted){
            Supperstructure.getInstance().setIntakeDesired(false);
            Supperstructure.getInstance().setPrepToShootDesired(false);
        }
        if(driveForward[idx].isDone()){
            idx++;
            driveForward[idx].userStart();
        }
        
        driveForward[idx].userUpdate();
    }
    
    @Override
    public void userForceStop() {
        Supperstructure.getInstance().setIntakeDesired(false);
        Supperstructure.getInstance().setPrepToShootDesired(false);
        for(int i=0; i<len;i++){
            driveForward[i].userForceStop();
        }
    }

    @Override
    public boolean isTriggered() {
        return driveForward[idx].isTriggered();
    }

    @Override
    public boolean isDone() {
        return driveForward[len].isDone();
    }

    public static void main(String[] args) {
        AutoEventNoStealSteakB autoEvent = new AutoEventNoStealSteakB(4.0); //time is for intk
		//TODO
		System.out.println("Done");
    }
}