package frc.robot.VisionProc;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class CasseroleVision extends VisionCamera {

    private static CasseroleVision instance = null;

    public static synchronized CasseroleVision getInstance() {
        if(instance == null)
            instance = new CasseroleVision();
        return instance;
    }

    NetworkTableEntry proc_duration_sec_nt;
    NetworkTableEntry framerate_fps_nt;
    NetworkTableEntry targetVisible_nt;
    NetworkTableEntry targetAngle_deg_nt;
    NetworkTableEntry targetPosStable_nt;
    
    double proc_duration_sec;
    double framerate_fps;
    double targetAngle_deg;
    boolean targetVisible;
    boolean targetPosStable;

    boolean visionOnline;
    long visionUpdatedTime;

    private CasseroleVision(){
        NetworkTableInstance.getDefault().setUpdateRate(0.01);
        NetworkTable table = NetworkTableInstance.getDefault().getTable("VisionData");
        proc_duration_sec_nt = table.getEntry("proc_duration_sec");
        framerate_fps_nt = table.getEntry("framerate_fps");
        targetVisible_nt = table.getEntry("targetVisible");
        targetAngle_deg_nt = table.getEntry("targetAngle_deg");
        targetPosStable_nt = table.getEntry("targetPosStable");
    }

    @Override
    public void update() {
        //read values periodically
        proc_duration_sec = proc_duration_sec_nt.getDouble(-1.0);
        framerate_fps = framerate_fps_nt.getDouble(-1.0);
        targetVisible = targetVisible_nt.getBoolean(false);
        targetPosStable = targetPosStable_nt.getBoolean(false);
        targetAngle_deg = targetAngle_deg_nt.getDouble(-1.0);
        visionUpdatedTime = targetAngle_deg_nt.getLastChange();
        
        if(framerate_fps == -1.0){
            visionOnline = false;
            targetVisible = false;
        } else {
            visionOnline = true;
        }
    }

    @Override
    public double getTgtAngle() {
        return targetAngle_deg;
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
        return targetAngle_deg;
    }

	@Override
	public boolean isTgtVisible() {
		return targetVisible;
	}

	@Override
	public boolean isVisionOnline() {
		return visionOnline;
    }
    
    @Override
    public boolean isTargetStable(){
        return targetPosStable;
    }

}