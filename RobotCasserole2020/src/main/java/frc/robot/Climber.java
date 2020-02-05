package frc.robot;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Spark;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.robot.HumanInterface.OperatorController;


public class Climber{
    private static Climber inst = null;
    public static synchronized Climber getInstance() {
        if (inst == null)
            inst = new Climber();
        return inst;
    }


    Spark climberMotor;
    TwoWireParitySwitch upperLimitSwitch;
    TwoWireParitySwitch lowerLimitSwitch;
    Solenoid climbLocker; 
    boolean climbEnabled;
    double climbCMD=0;
    Calibration climberSpeed;
    Calibration climberSpeedOffset;

    Signal climberCMDSignal;
    Signal climberMotorCmdSignal;
    Signal climbMotorCurrentSignal;
    Signal climberUpperLimitSignal;
    Signal climberLowerLimitSignal;
    Signal limitSwitchesReadingRightSig;

    TwoWireParitySwitch.SwitchState upperLSVal;
    TwoWireParitySwitch.SwitchState lowerLSVal;

    boolean lowerLimitSwitchFaulted;
    boolean upperLimitSwitchFaulted;

    double initialPulseCounter;

    public Climber(){
        climberMotor = new Spark(RobotConstants.CLIMBER_SPARK_LEFT_ID);
        upperLimitSwitch = new TwoWireParitySwitch(RobotConstants.CLIMBER_LIMIT_UPPER_NO_ID, RobotConstants.CLIMBER_LIMIT_UPPER_NC_ID);
        lowerLimitSwitch = new TwoWireParitySwitch(RobotConstants.CLIMBER_LIMIT_LOWER_NO_ID, RobotConstants.CLIMBER_LIMIT_LOWER_NC_ID);
        climbLocker = new Solenoid(RobotConstants.CLIMBER_SOLENOID_ID);
        climberSpeed=new Calibration("Climber Max Speed", 1, 0, 1);
        climberSpeedOffset=new Calibration("Climber Stopped Motor Offset Speed", 0.0, 0, 1);
        climberCMDSignal= new Signal("Climber Input Command","cmd");
        climberMotorCmdSignal= new Signal("Climber Motor Command","cmd");
        climbMotorCurrentSignal= new Signal("Climber Motor Current","A");
        climberUpperLimitSignal= new Signal("Climber Upper Limit Switch","state");
        climberLowerLimitSignal= new Signal("Climber Lower Limit Switch","state");
    }

    public void update(){
        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;
        double motorCmd = 0;

        climbEnabled = OperatorController.getInstance().getClimbEnableCmd();
        climbCMD = OperatorController.getInstance().getClimbSpeedCmd();

        upperLSVal = upperLimitSwitch.get();
        lowerLSVal = lowerLimitSwitch.get();

        upperLimitSwitchFaulted = (upperLSVal == TwoWireParitySwitch.SwitchState.Broken) ;
        lowerLimitSwitchFaulted = (lowerLSVal == TwoWireParitySwitch.SwitchState.Broken) ;

        boolean canClimb = !upperLimitSwitchFaulted && !lowerLimitSwitchFaulted;

        if(climbEnabled && canClimb){
            
            climbLocker.set(true);
            motorCmd = 0;
            initialPulseCounter = 4;
        
        } else {

            climbLocker.set(false);
            
            if(initialPulseCounter > 0) {
                //Initial Pulse for ensuring the latch fully disengages.
                motorCmd = -0.5;
                initialPulseCounter--;
            } else {

                if (upperLSVal == TwoWireParitySwitch.SwitchState.Pressed){
                    motorCmd = Math.min(0,climbCMD);
                } else if (lowerLSVal == TwoWireParitySwitch.SwitchState.Pressed){
                    motorCmd = Math.max(0,climbCMD);
                } else {
                    motorCmd = climbCMD;
                }

                motorCmd *= climberSpeed.get();
                motorCmd += climberSpeedOffset.get();
                
            }
        }

        climberMotor.set(motorCmd);

        climberCMDSignal.addSample(sampleTimeMs, climbCMD);
        climberMotorCmdSignal.addSample(sampleTimeMs, motorCmd);
        climbMotorCurrentSignal.addSample(sampleTimeMs, CasserolePDP.getInstance().getCurrent(RobotConstants.CLIMBER_SPARK_LEFT_PDP_ID));
        climberUpperLimitSignal.addSample(sampleTimeMs, upperLimitSwitch.get().value);
        climberLowerLimitSignal.addSample(sampleTimeMs, lowerLimitSwitch.get().value);

    }

    public boolean isLowerLimitSwitchFaulted(){
        return lowerLimitSwitchFaulted;
    }

    public boolean isUpperLimitSwitchFaulted(){
        return upperLimitSwitchFaulted;
    }

}
