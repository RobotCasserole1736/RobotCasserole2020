package frc.robot.BallHandling;

import edu.wpi.first.wpilibj.AnalogInput;
import frc.lib.DataServer.Signal;
import frc.lib.SignalMath.AveragingFilter;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;

public class BallDistanceSensor{

    /* Singleton infratructure*/
    private static BallDistanceSensor inst = null;
    public static synchronized BallDistanceSensor getInstance() {
        if (inst == null)
            inst = new BallDistanceSensor();
        return inst;
    }

    
    AnalogInput sensorInput;

    double detectedDistance_in;
    boolean distAvailable;
    double sensorVoltage_V;

    Signal sensorVoltageSig;
    Signal detectedDistancesSig;
    Signal distAvailSig;

    AveragingFilter voltFilter;

    private BallDistanceSensor(){
        distAvailable = false;
        sensorInput = new AnalogInput(RobotConstants.BALL_DIST_SENSOR_PORT);
        
        sensorVoltageSig = new Signal("Distance Sensor Raw Voltage", "V");
        detectedDistancesSig = new Signal("Distance Sensor Distance", "in");
        distAvailSig = new Signal("Distance Sensor Available", "bool");
        voltFilter = new AveragingFilter(10, 0);
    }

    public void update(){
        sensorVoltage_V = sensorInput.getVoltage();

        detectedDistance_in = voltFilter.filter(voltsToInches(sensorVoltage_V));

        distAvailable = true; //TODO - better range/error handling.

        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        sensorVoltageSig.addSample(sampleTimeMS, sensorVoltage_V);
        detectedDistancesSig.addSample(sampleTimeMS, detectedDistance_in);
        distAvailSig.addSample(sampleTimeMS, distAvailable);
    }

    double voltsToInches(double voltsIn){
        return voltsIn * 512/5.0; // Per https://www.maxbotix.com/documents/LV-MaxSonar-EZ_Datasheet.pdf - output is Vcc/512 volts per inch.
    }

    public double getDistance_in(){
        return 0;
    }
}