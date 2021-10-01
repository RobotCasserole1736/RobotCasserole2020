package frc.robot.VisionProc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;

public class CasseroleVision {

    private static CasseroleVision instance = null;

    public static synchronized CasseroleVision getInstance() {
        if(instance == null)
            instance = new CasseroleVision();
        return instance;
    }


    double proc_duration_sec;
    double framerate_fps;
    double targetAngle_deg;
    
    boolean targetVisible;
    boolean targetPosStable;
    boolean visionOnline;
    long visionUpdatedTime;


    Signal targetAngleSignal;
    Signal targetVisibleSignal;
    Signal targetStableSignal;
    Signal cameraFramerateSignal;
    Signal cameraDurationSignal;
    Signal visionOnlineSignal;

    PhotonCamera camera;

    private CasseroleVision(){
        NetworkTableInstance.getDefault().setUpdateRate(0.01); //SPEEEEEEEEEEED
        NetworkTable table = NetworkTableInstance.getDefault().getTable("VisionData");

        targetAngleSignal= new Signal("Vision Raspberry Pi Angle","deg");
        targetVisibleSignal= new Signal("Vision Raspberry Pi Visible Target","bool");
        targetStableSignal= new Signal("Vision Raspberry Pi Stable Target","bool");
        cameraFramerateSignal= new Signal("Vision Raspberry Pi Framerate","fps");
        cameraDurationSignal= new Signal("Vision Raspberry Pi Duration","sec");
        visionOnlineSignal= new Signal("Vision Raspberry Pi Vision System Online","bool");

        camera = new PhotonCamera("photonvision");
    }

    public void update() {
        //read values periodically
        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;

        var result = camera.getLatestResult();

        proc_duration_sec = result.getLatencyMillis() / 1000.0;
        framerate_fps = -1.0;
        targetVisible = result.hasTargets();
        visionUpdatedTime = Timer.getFPGATimestamp() - result.getLatencyMillis() / 1000.0;


        if(targetVisible){
            var target = result.getBestTarget();
            targetPosStable = true;
            targetAngle_deg = target.getYaw();
        } else {
            targetPosStable = false;
            targetAngle_deg = 0.0;
        }

        

        visionOnline = true;


        targetAngleSignal.addSample(sampleTimeMs, targetAngle_deg);
        targetVisibleSignal.addSample(sampleTimeMs, targetVisible);
        targetStableSignal.addSample(sampleTimeMs, targetPosStable);
        cameraFramerateSignal.addSample(sampleTimeMs, framerate_fps);
        cameraDurationSignal.addSample(sampleTimeMs, proc_duration_sec);
        visionOnlineSignal.addSample(sampleTimeMs, visionOnline);

    }

    private boolean convertDoubletoBoolean(double inDouble){
        if(inDouble==0){
            return false;
        }else{
            return true;
        }
    }

    public double getTgtAngle() {
        return targetAngle_deg;
    }

    public double getTgtGeneralAngle() {
        return targetAngle_deg;
    }

	public boolean isTgtVisible() {
	    return targetVisible;
	}

	public boolean isVisionOnline() {
		return visionOnline;
    }
    
    public boolean isTargetStable(){
        return targetPosStable;
    }

    public void TakeAPicture(){
        camera.takeInputSnapshot();
        camera.takeOutputSnapshot();
    }

    public void setInnerGoalAsTarget(boolean input){
        //TBD.... nothing I guess?
    }

}