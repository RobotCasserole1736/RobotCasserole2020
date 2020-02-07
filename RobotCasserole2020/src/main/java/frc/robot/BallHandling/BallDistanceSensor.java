package frc.robot.BallHandling;

import com.playingwithfusion.TimeOfFlight;
import edu.wpi.first.wpilibj.AnalogInput;
import frc.lib.DataServer.Signal;
import frc.lib.SignalMath.AveragingFilter;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;
import frc.robot.RobotSimMode;

public class BallDistanceSensor{

    /* Singleton infratructure*/
    private static BallDistanceSensor inst = null;
    public static synchronized BallDistanceSensor getInstance() {
        if (inst == null)
            inst = new BallDistanceSensor();
        return inst;
    }

    
    TimeOfFlight tofSensor;

    double detectedDistance_in;
    boolean distAvailable;
    double sensorDistance_in;

    Signal sensorVoltageSig;
    Signal detectedDistancesSig;
    Signal distAvailSig;

    AveragingFilter voltFilter;

    private BallDistanceSensor(){
        distAvailable = false;

        if(!RobotSimMode.getInstance().runSimulation()){
            tofSensor = new TimeOfFlight(RobotConstants.TOF_CAN_ID);
            tofSensor.setRangingMode(TimeOfFlight.RangingMode.Short, 24);
        }
        
        sensorVoltageSig = new Signal("Distance Sensor Raw Voltage", "V");
        detectedDistancesSig = new Signal("Distance Sensor Distance", "in");
        distAvailSig = new Signal("Distance Sensor Available", "bool");
        voltFilter = new AveragingFilter(2, 0);
    }

    public void update(){
        double tempDistance = 0;
        boolean rangeValid = false;

        if(!RobotSimMode.getInstance().runSimulation()){
            tempDistance = tofSensor.getRange()/25.40; //Convert from mm to inches
            rangeValid = tofSensor.isRangeValid();
        }

        if(rangeValid){
            detectedDistance_in = voltFilter.filter(tempDistance);
            distAvailable = true;
        } else {
            distAvailable = false;
        }

        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        detectedDistancesSig.addSample(sampleTimeMS, detectedDistance_in);
        distAvailSig.addSample(sampleTimeMS, distAvailable);
    }

    public double getDistance_in(){
        return detectedDistance_in;
    }
}