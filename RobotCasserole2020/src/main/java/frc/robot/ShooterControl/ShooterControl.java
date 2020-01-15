/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.ShooterControl;

import edu.wpi.first.wpilibj.RobotBase;

/**
 * Add your docs here.
 */
public abstract class ShooterControl {

    /* Allowable shooter operation modes*/
    public enum ShooterCtrlMode {
        SpoolUp(0),   
        HoldSpeed(1),  
        Stop(2);

        public final int value;
        private ShooterCtrlMode(int value) {
            this.value = value;
        }
    }
    

    boolean run;

    private static ShooterControl instance = null;
	public static synchronized ShooterControl getInstance() {
		if(instance == null){
            //On init, choose whether we want a real or fake drivetrain
            if(RobotBase.isReal()){
                instance = new RealShooterControl(); 
            } else {
                instance = new ImaginaryShooterControl();
            }
        }
		return instance;
    }

    public void setRun(final boolean runCmd) {
        run = runCmd;
    }

    public abstract void update();

    public abstract boolean isUnderLoad();
}
