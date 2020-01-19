package frc.robot.VisionProc;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class CustomPiVisionThingy extends VisionCamera {

    private static CustomPiVisionThingy instance = null;

    public static synchronized CustomPiVisionThingy getInstance() {
		if(instance == null)
			instance = new CustomPiVisionThingy();
        return instance;
    }

    NetworkTableEntry proc_duration_sec_nt;
	NetworkTableEntry framerate_fps_nt;
	
	double proc_duration_sec;
	double framerate_fps;

    private CustomPiVisionThingy(){
		NetworkTableInstance.getDefault().setUpdateRate(0.01);
        NetworkTable table = NetworkTableInstance.getDefault().getTable("VisionData");
        proc_duration_sec_nt = table.getEntry("proc_duration_sec");
        framerate_fps_nt = table.getEntry("framerate_fps");


    }

	@Override
	public void update() {
        //read values periodically
        proc_duration_sec = proc_duration_sec_nt.getDouble(0.0);
        framerate_fps = framerate_fps_nt.getDouble(0.0);	
	}

	@Override
	public double getTgtAngle() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTgtPositionX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTgtPositionY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTgtGeneralAngle() {
		// TODO Auto-generated method stub
		return 0;
	}

}