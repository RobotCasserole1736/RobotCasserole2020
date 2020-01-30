package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SerialPort.StopBits;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;


public class Climber{
    private static Climber inst = null;
    public static synchronized Climber getInstance() {
        if (inst == null)
            inst = new Climber();
        return inst;
    }
    public enum SwitchState {
        Pressed(0),
        NotPressed(1),
        Broken(2);

        public final int value;

        private SwitchState(int value) {
            this.value = value;
        }
    }

    Spark leftClimber;
    DigitalInput upperLimitSwitch;
    DigitalInput lowerLimitSwitch;
    Solenoid climbLocker; 
    boolean climbEnabled;
    double climbCMD=0;
    Calibration climberSpeed;
    Signal climberCMDSignal;
    Signal climberLeftCurrentSignal;
    Signal climberRightCurrentSignal;
    Signal climberUpperLimitSignal;
    Signal climberLowerLimitSignal;
    Signal limitSwitchesReadingRightSig;

    SwitchState limitSwitchState;
    boolean canClimb;

    public Climber(){
        leftClimber = new Spark(RobotConstants.CLIMBER_SPARK_LEFT_ID);
        upperLimitSwitch = new DigitalInput(RobotConstants.CLIMBER_LIMIT_UPPER_ID);
        lowerLimitSwitch = new DigitalInput(RobotConstants.CLIMBER_LIMIT_LOWER_ID);
        climbLocker = new Solenoid(RobotConstants.CLIMBER_SOLENOID_ID);
        climberSpeed=new Calibration("Climber Max Speed", 1);
        climberCMDSignal= new Signal("Climber Command","CMD");
        climberLeftCurrentSignal= new Signal("Left Climber Current","Amp");
        climberRightCurrentSignal= new Signal("Right Climber Current","Amp");
        climberUpperLimitSignal= new Signal("Climber Upper Limit Switch","boolean");
        climberLowerLimitSignal= new Signal("Climber Lower Limit Switch","boolean");
        limitSwitchesReadingRightSig = new Signal("What Enum Value are the Limit Switches Responding With","State");
    }
    public void update(){
        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;
        
        canClimber();
            if(canClimb) {
            if(climbEnabled){
                climbLocker.set(true);
                setClimber(0);
            }else{
                climbLocker.set(false);
                if (upperLimitSwitch.get()){
                    setClimber(Math.min(0,climbCMD));
                }else if (lowerLimitSwitch.get()){
                    setClimber(Math.min(0,climbCMD));
                }else{
                    setClimber(climbCMD);
                }
        }

        }
        climberCMDSignal.addSample(sampleTimeMs, climbCMD);
        climberLeftCurrentSignal.addSample(sampleTimeMs, CasserolePDP.getInstance().getCurrent(RobotConstants.CLIMBER_SPARK_LEFT_PDP_ID));
        climberRightCurrentSignal.addSample(sampleTimeMs, CasserolePDP.getInstance().getCurrent(RobotConstants.CLIMBER_SPARK_RIGHT_PDP_ID));
        climberUpperLimitSignal.addSample(sampleTimeMs, upperLimitSwitch.get());
        climberLowerLimitSignal.addSample(sampleTimeMs, lowerLimitSwitch.get());
        limitSwitchesReadingRightSig.addSample(sampleTimeMs, limitSwitchState.value);

    }
    public void defLimitSwitchStates() {
        if(upperLimitSwitch.get() != lowerLimitSwitch.get()) {
            if(upperLimitSwitch.get() == true || lowerLimitSwitch.get() == true) {
                limitSwitchState = SwitchState.Pressed;        
            }else {
                limitSwitchState = SwitchState.NotPressed;}
        }else {
            limitSwitchState = SwitchState.Broken;
        }
    }

    
    public void setSpeed(double cmd){
        climbCMD=cmd;
    }

    public void setClimberEnable(boolean enabled){
        climbEnabled = enabled;
    }
    public void canClimber() {
        if(limitSwitchState == SwitchState.Broken) {
            canClimb = true;
        }else{
            canClimb = false;
        }
    }


    private void setClimber(double cmd){
        leftClimber.set(cmd*climberSpeed.get());
    }
}