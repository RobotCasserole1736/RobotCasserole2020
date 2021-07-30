package frc.robot.BallHandling;

import com.playingwithfusion.TimeOfFlight;

import frc.lib.Signal.Annotations.Signal;
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

    @Signal
    double detectedDistance_in;
    @Signal
    boolean distAvailable;
    @Signal
    double sensorDistance_in;

    AveragingFilter voltFilter;

    private BallDistanceSensor(){
        distAvailable = false;

        if(!RobotSimMode.getInstance().runSimulation()){
            tofSensor = new TimeOfFlight(RobotConstants.TOF_CAN_ID);
            tofSensor.setRangingMode(TimeOfFlight.RangingMode.Short, 24);
            tofSensor.setRangeOfInterest(6, 6, 10, 10);
        }
        
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

    }

    public double getDistance_in(){
        return detectedDistance_in;
    }
}