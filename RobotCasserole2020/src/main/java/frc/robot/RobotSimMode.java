package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

public class RobotSimMode{
    
    private static RobotSimMode instance = null;
	public static synchronized RobotSimMode getInstance() {
		if(instance == null){
            instance = new RobotSimMode();
        }
		return instance;
    }

    private boolean runSim = false;
    final boolean FORCE_SIM = false;

    private RobotSimMode(){
        runSim = FORCE_SIM || RobotBase.isSimulation();
    }



    public boolean runSimulation(){
        return runSim;
    }
}