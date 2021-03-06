/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.ShooterControl;

import frc.lib.Calibration.Calibration;
import frc.lib.Util.ExecutionTimeTracker;
import frc.robot.RobotSimMode;

/**
 * Add your docs here.
 */
public abstract class ShooterControl {

    /* Allowable shooter operation modes*/
    public enum ShooterCtrlMode {
        Stop(0),
        Accelerate(1),   
        Stabilize(2),   
        HoldForShot(3),
        Shooting(4),
        JustGonnaSendEm(5);

        public final int value;
        private ShooterCtrlMode(int value) {
            this.value = value;
        }
    }

    public enum ShooterRunCommand {
        ShotFar(0),   
        ShotClose(1),
        Eject(2),
        Stop(3);

        public final int value;
        private ShooterRunCommand(int value) {
            this.value = value;
        }
    }

    ShooterRunCommand runCommand = ShooterRunCommand.Stop;

    Calibration shooterRPMSetpointFar;
    Calibration shooterSendEmVoltage;

    
    final double SHOT_ADJUST_STEP_RPM = 25.0;
    double shotAdjustmentRPM = 0;
    boolean shotAdjustmentChanged = false;

    public ExecutionTimeTracker timeTracker;

    private static ShooterControl instance = null;
	public static synchronized ShooterControl getInstance() {
		if(instance == null){
            //On init, choose whether we want a real or fake drivetrain
            if(RobotSimMode.getInstance().runSimulation()){
                instance = new ImaginaryShooterControl();
            } else {
                instance = new RealShooterControl();
            }
        }
		return instance;
    }

    public void commonInit(){
        timeTracker = new ExecutionTimeTracker("ShooterControl", 0.03);

    }

    public void setRun(ShooterRunCommand runCmd) {
        runCommand = runCmd;
    }

    public abstract void update();

    public abstract boolean isUnderLoad();

    public abstract double getSpeedRPM();

    public abstract ShooterCtrlMode getShooterCtrlMode();

    public abstract void updateGains(boolean forceChange);

    public abstract int getShotCount();


    public void incrementSpeedSetpoint() {
        shotAdjustmentRPM += SHOT_ADJUST_STEP_RPM;
        shotAdjustmentChanged = true;
    }

    public void decrementSpeedSetpoint() {
        shotAdjustmentRPM -= SHOT_ADJUST_STEP_RPM;
        shotAdjustmentChanged = true;
    }

    public abstract double getAdjustedSetpointRPM();
    
}
