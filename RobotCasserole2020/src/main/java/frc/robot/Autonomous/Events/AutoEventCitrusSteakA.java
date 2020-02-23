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
public class AutoEventCitrusSteakA extends AutoEvent {
    PathPlannerAutoEvent[] driveForward;

    int idx;
    int len;
    //stuff for Intake
	double intkPrepDuration_s;
	double intkPrepEndTime;
	boolean intkPrepCompleted = true;

    

    private final Waypoint[] citrus_waypoints_ft_pt0 = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(5.9,   0.0,  Pathfinder.d2r(0)),
        new Waypoint(8.5,1.7,Pathfinder.d2r(65))
    };

    private final Waypoint[] citrus_waypoints_ft_pt1 = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(-9.0,1.0,  Pathfinder.d2r(25)),
        new Waypoint(-9-(14*0.906307787), 1-(14*0.422618262),  Pathfinder.d2r(25))
    };
    // private final Waypoint[] citrus_waypoints_ft_pt2 = new Waypoint[] {
    //     new Waypoint(0,      0,  Pathfinder.d2r(0)),
    //     new Waypoint(-7.5, 0,  Pathfinder.d2r(0))
    // };
    private final Waypoint[] citrus_waypoints_ft_pt2 = new Waypoint[] {
        new Waypoint(0,      0,  Pathfinder.d2r(0)),
        new Waypoint(0, 3.6,  Pathfinder.d2r(90))
    };


    public AutoEventCitrusSteakA(double intkPrepDuration_s_in) {
        intkPrepDuration_s = intkPrepDuration_s_in;
        idx=0;
        driveForward=new PathPlannerAutoEvent[3];
        driveForward[0] = new PathPlannerAutoEvent(citrus_waypoints_ft_pt0, false,12,6);
        driveForward[1] = new PathPlannerAutoEvent(citrus_waypoints_ft_pt1, true,12,6);
        driveForward[2] = new PathPlannerAutoEvent(citrus_waypoints_ft_pt2, true,12,6);
        len=2;
        
        
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
        AutoEventCitrusSteakA autoEvent = new AutoEventCitrusSteakA(4.0); //time is for intk
		//TODO
		System.out.println("Done");
    }
}