package frc.robot;

public class TelemetryUpdate{
    private static TelemetryUpdate inst = null;
    public static synchronized TelemetryUpdate getInstance() {
        if (inst == null)
            inst = new TelemetryUpdate();
        return inst;
    }

    public TelemetryUpdate(){

    }       
    public void update(){
        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;
        

    }

}