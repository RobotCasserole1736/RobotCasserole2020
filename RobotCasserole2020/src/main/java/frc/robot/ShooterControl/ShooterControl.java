/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.ShooterControl;

import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.robot.RobotSimMode;

/**
 * Add your docs here.
 */
public abstract class ShooterControl {

    /* Allowable shooter operation modes*/
    public enum ShooterCtrlMode {
        SpoolUp(0),   
        HoldForShot(1),
        Shooting(2),
        Stop(-1);

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

    ShooterRunCommand run;

    Calibration shooterRPMSetpointFar;
    Calibration shooterRPMSetpointClose;

    Signal rpmDesiredSig;
    Signal rpmActualSig;
    Signal shooterStateCommandSig;
    Signal shooterControlModeSig;

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
        rpmDesiredSig = new Signal("Shooter Desired Speed", "RPM");
        rpmActualSig = new Signal("Shooter Actual Speed", "RPM");
        shooterStateCommandSig = new Signal("Shooter Input Mode Command", "state");
        shooterControlModeSig = new Signal("Shooter Wheel Current Control Mode", "state");
    }

    public void setRun(ShooterRunCommand runCmd) {
        run = runCmd;
    }

    public abstract void update();

    public abstract boolean isUnderLoad();

    public abstract double getSpeedRPM();

    public abstract ShooterCtrlMode getShooterCtrlMode();

    public abstract void updateGains(boolean forceChange);

    public abstract int getShotCount();
    
}
