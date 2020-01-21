package frc.robot.Autonomous.Events;

import frc.lib.AutoSequencer.AutoEvent;
import frc.lib.PathPlanner.FalconPathPlanner;
import frc.lib.PathPlanner.PathPlannerAutoEvent;

/**
 * go to scale on left.
 */
public class AutoEventCollectSteak extends AutoEvent {
    PathPlannerAutoEvent driveForward;
    
	private final double[][] waypoints_inches = new double[][] {
        {0, 0},
        {0, 50},
        {-137, 100},
        {-137, 123}
        
	};
	
	private final double time = 2.0;
    
	public AutoEventCollectSteak() {
        driveForward = new PathPlannerAutoEvent(waypoints_inches, time, false, 1, 0.3, 0.01, 0.9);
	}
    
	@Override
	public void userUpdate() {
        driveForward.userUpdate();
    }
		// shotCTRL.setDesiredShooterState(ShooterStates.PREP_TO_ollectSteak
        @Override
        public void userForceStop() {
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
        
        @Override
        public void userStart() {
            driveForward.userStart();
        }
        public static void main(String[] args) {
            AutoEventCollectSteak autoEvent = new AutoEventCollectSteak();
            FalconPathPlanner.plotPath(autoEvent.driveForward.path);
        }
    }