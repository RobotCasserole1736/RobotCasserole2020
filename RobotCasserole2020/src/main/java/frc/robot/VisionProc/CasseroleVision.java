package frc.robot.VisionProc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;

public class CasseroleVision {

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
    NetworkTableEntry FuzzyPickles;
    NetworkTableEntry InMatch;
    NetworkTableEntry MatchNumber;
    NetworkTableEntry InnerAim;
    NetworkTableEntry Heartbeat;

    double camHbeatVal = 0;
    double camHbeatPrev = 0;
    final int CAM_HBEAT_LIMIT_LOOPS = 1500; //3 second timeout
    int camHbeatCounter = CAM_HBEAT_LIMIT_LOOPS;
    
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



    private CasseroleVision(){
        NetworkTableInstance.getDefault().setUpdateRate(0.01); //SPEEEEEEEEEEED
        NetworkTable table = NetworkTableInstance.getDefault().getTable("VisionData");

        //Camera -> RIO
        proc_duration_sec_nt = table.getEntry("proc_duration_sec");
        framerate_fps_nt = table.getEntry("framerate_fps");
        targetVisible_nt = table.getEntry("targetVisible");
        targetAngle_deg_nt = table.getEntry("targetAngle_deg");
        targetPosStable_nt = table.getEntry("targetPosStable");

        //RIO -> Camera
        FuzzyPickles = table.getEntry("Fuzzy Pickles");
        InMatch = table.getEntry("InMatch");
        MatchNumber = table.getEntry("MatchNumber");
        InnerAim = table.getEntry("InnerAim");
        Heartbeat = table.getEntry("Heartbeat");

        targetAngleSignal= new Signal("Vision Raspberry Pi Angle","deg");
        targetVisibleSignal= new Signal("Vision Raspberry Pi Visible Target","bool");
        targetStableSignal= new Signal("Vision Raspberry Pi Stable Target","bool");
        cameraFramerateSignal= new Signal("Vision Raspberry Pi Framerate","fps");
        cameraDurationSignal= new Signal("Vision Raspberry Pi Duration","sec");
        visionOnlineSignal= new Signal("Vision Raspberry Pi Vision System Online","bool");

        updateMatchData();
    }

    private String getDateTimeString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        df.setTimeZone(TimeZone.getTimeZone("US/Central"));
        return df.format(new Date());
    }

    private void updateMatchData(){
        InMatch.setBoolean(true);
        MatchNumber.setString(DriverStation.getInstance().getEventName()+"_"
        +DriverStation.getInstance().getMatchType()+"_"
        +Integer.toString(DriverStation.getInstance().getMatchNumber())+"_"
        +  getDateTimeString());
    }


    public void update() {
        //read values periodically
        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;
        proc_duration_sec = proc_duration_sec_nt.getDouble(-1.0);
        framerate_fps = framerate_fps_nt.getDouble(-1.0);
        targetVisible = convertDoubletoBoolean(targetVisible_nt.getDouble(0.0));
        targetPosStable = convertDoubletoBoolean(targetPosStable_nt.getDouble(0.0));
        targetAngle_deg = targetAngle_deg_nt.getDouble(-1.0);
        visionUpdatedTime = targetAngle_deg_nt.getLastChange();
        
        camHbeatPrev = camHbeatVal;
        camHbeatVal = Heartbeat.getDouble(0.0);

        //Expect the vision process software to update a heartbeat periodically.
        // Declare vision offline if that heartbeat gets stuck.
        if(camHbeatPrev != camHbeatVal){
            camHbeatCounter = CAM_HBEAT_LIMIT_LOOPS;
            visionOnline = true;
        } else {
            if(camHbeatCounter > 0){
                camHbeatCounter--;
                visionOnline = true;
            } else {
                visionOnline = false;
                targetVisible = false;
            }
        }

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
        FuzzyPickles.setBoolean(true);
    }

    public void setInnerGoalAsTarget(boolean input){
        InnerAim.setBoolean(input);
    }

}