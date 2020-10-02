package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Spark;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Annotations.Signal;
import frc.robot.HumanInterface.OperatorController;


public class Climber{
    private static Climber inst = null;
    public static synchronized Climber getInstance() {
        if (inst == null)
            inst = new Climber();
        return inst;
    }

    //Climber uses one PWM port, split into two physical sparks.
    //This means one output, but two current measurements
    Spark climberMotor;
    TwoWireParitySwitch upperLimitSwitch;
    TwoWireParitySwitch lowerLimitSwitch;
    Solenoid climbLocker; 
    @Signal
    boolean climbEnabled;
    @Signal
    double climbCMD=0;
    Calibration climberSpeed;
    Calibration climberSpeedOffset;

    TwoWireParitySwitch.SwitchState upperLSVal;
    TwoWireParitySwitch.SwitchState lowerLSVal;

    boolean lowerLimitSwitchFaulted;
    boolean upperLimitSwitchFaulted;
    boolean upperLimitSwitchPressed;
    boolean lowerLimitSwitchPressed;
    boolean canClimb;

    double initialPulseCounter;

    public Climber(){
        climberMotor = new Spark(RobotConstants.CLIMBER_SPARK_PORT);
        climberMotor.setInverted(true);
        upperLimitSwitch = new TwoWireParitySwitch(RobotConstants.CLIMBER_LIMIT_UPPER_NO_DIO_PORT, RobotConstants.CLIMBER_LIMIT_UPPER_NC_DIO_PORT);
        lowerLimitSwitch = new TwoWireParitySwitch(RobotConstants.CLIMBER_LIMIT_LOWER_NO_DIO_PORT, RobotConstants.CLIMBER_LIMIT_LOWER_NC_DIO_PORT);
        climbLocker = new Solenoid(RobotConstants.CLIMBER_SOLENOID_PCM_PORT);
        climberSpeed=new Calibration("Climber Max Speed", 1, 0, 1);
        climberSpeedOffset=new Calibration("Climber Stopped Motor Offset Speed", 0.0, 0, 1);
    }

    @Signal
    double motorCmd = 0;

    public void update(){

        if(DriverStation.getInstance().isDisabled()){
            climbEnabled = false;
            climbCMD = 0;
        }else{
            climbEnabled = OperatorController.getInstance().getClimbEnableCmd();
            climbCMD = OperatorController.getInstance().getClimbSpeedCmd();
        }

        upperLSVal = upperLimitSwitch.get();
        lowerLSVal = lowerLimitSwitch.get();

        upperLimitSwitchFaulted = (upperLSVal == TwoWireParitySwitch.SwitchState.Broken);
        lowerLimitSwitchFaulted = (lowerLSVal == TwoWireParitySwitch.SwitchState.Broken);

        canClimb = !upperLimitSwitchFaulted && !lowerLimitSwitchFaulted;

        upperLimitSwitchPressed = (upperLSVal == TwoWireParitySwitch.SwitchState.Pressed);
        lowerLimitSwitchPressed = (lowerLSVal == TwoWireParitySwitch.SwitchState.Pressed);

        if(!climbEnabled){
            
            climbLocker.set(false);
            motorCmd = 0;
            initialPulseCounter = 4;
        
        } else {

            climbLocker.set(true);
            
            if(initialPulseCounter > 0) {
                //Initial Pulse for ensuring the latch fully disengages.
                motorCmd = 0.5;
                initialPulseCounter--;
            } else {

                if (upperLSVal == TwoWireParitySwitch.SwitchState.Pressed){
                    motorCmd = Math.max(0,climbCMD);
                } else if (lowerLSVal == TwoWireParitySwitch.SwitchState.Pressed){
                    motorCmd = Math.min(0,climbCMD);
                } else {
                    motorCmd = climbCMD;
                }

                motorCmd *= climberSpeed.get();
                motorCmd += climberSpeedOffset.get();
                
            }
        }

        climberMotor.set(motorCmd);
        readCurrents();

    }

    public boolean isLowerLimitSwitchFaulted(){
        return lowerLimitSwitchFaulted;
    }

    public boolean isUpperLimitSwitchFaulted(){
        return upperLimitSwitchFaulted;
    }

    public boolean isUpperLimitSwitchPressed(){
        return upperLimitSwitchPressed;
    }

    public boolean isLowerLimitSwitchPressed(){
        return lowerLimitSwitchPressed;
    }

    public boolean canClimb(){
        return canClimb;
    }

    @Signal
    double motor1Current = 0;
    @Signal
    double motor2Current = 0;

    public void readCurrents(){
        motor1Current = CasserolePDP.getInstance().getCurrent(RobotConstants.CLIMBER_SPARK_1_PDP_CHANNEL);
        motor2Current = CasserolePDP.getInstance().getCurrent(RobotConstants.CLIMBER_SPARK_2_PDP_CHANNEL);
    }

}
