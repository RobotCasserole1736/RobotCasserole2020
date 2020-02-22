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
public class AutoEventCollectSteakPt2 extends AutoEvent {
    PathPlannerAutoEvent[] driveForward;

    int idx;
    int len;
    //stuff for Intake
    double intkSpeed;
	double intkDuration_s;
	double intkEndTime;
	boolean intkCompleted = true;

    // private final Waypoint[] waypoints_ft_pt2=new Waypoint[]{
    //     new Waypoint(0,      0,  Pathfinder.d2r(0)),
        
    // };

    private final Waypoint[] waypoints_ft_pt2 = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(12.3,   -2.7,  Pathfinder.d2r(-15)),
    };

    public AutoEventCollectSteakPt2(double intkDuration_s_in) {
        intkDuration_s = intkDuration_s_in;
        idx=0;
        driveForward=new PathPlannerAutoEvent[1];
        driveForward[0]=new PathPlannerAutoEvent(waypoints_ft_pt2, false,12,6);
        len=0;
        
    }

    @Override
    public void userStart() {
        intkEndTime = Timer.getFPGATimestamp() + intkDuration_s;
        intkCompleted = false;
        Supperstructure.getInstance().setIntakeDesired(true);

        driveForward[idx].userStart();
    }

    @Override
    public void userUpdate() {
        intkCompleted = (Timer.getFPGATimestamp() > intkEndTime);
		if (intkCompleted){
			Supperstructure.getInstance().setIntakeDesired(false);
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
        AutoEventCollectSteakPt2 autoEvent = new AutoEventCollectSteakPt2(4.0); //time is for intk
		//TODO
		System.out.println("Done");
    }
}