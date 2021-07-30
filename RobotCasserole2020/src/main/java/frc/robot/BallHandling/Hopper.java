package frc.robot.BallHandling;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import frc.robot.RobotConstants;
import frc.lib.Calibration.Calibration;
import frc.lib.Signal.Annotations.Signal;
import frc.robot.LoopTiming;
import frc.robot.Robot;

public class Hopper{

    /* Singleton infratructure*/
    private static Hopper inst = null;
    public static synchronized Hopper getInstance() {
        if (inst == null)
            inst = new Hopper();
        return inst;
    }
    @Signal
    HopperOpMode hopperOpMode;
    CANSparkMax hopperSparkLeft;
    CANSparkMax hopperSparkRight;

    @Signal
    double HopperSparkLeftCmd=0;
    @Signal
    double HopperSparkRightCmd=0;
    Calibration hopperFWDSpeed;
    Calibration hopperBWDSpeed;
    Conveyor conv = Conveyor.getInstance();

    /* All possible intake speed commands*/
    public enum HopperOpMode {
        Stop(0),     //No Motion
        Injest(1),   //Run balls toward the conveyer
        ClearJam(2), //Randomly (ish) change direction in an attempt to un-jam stuck balls
        Reverse(3);  //Run balls toward the intake

        public final int value;

        private HopperOpMode(int value) {
            this.value = value;
        }
    }


    private Hopper(){
        if(Robot.isReal()){
            //No REV support for sim :(
            hopperSparkLeft= new CANSparkMax(RobotConstants.HOPPER_NEO_LEFT_CAN_ID, MotorType.kBrushless);
            hopperSparkRight= new CANSparkMax(RobotConstants.HOPPER_NEO_RIGHT_CAN_ID, MotorType.kBrushless);
            hopperSparkLeft.restoreFactoryDefaults();
            hopperSparkRight.restoreFactoryDefaults();
            hopperSparkLeft.setIdleMode(IdleMode.kCoast);
            hopperSparkLeft.setIdleMode(IdleMode.kCoast);
            hopperSparkLeft.setSmartCurrentLimit(25);
            hopperSparkRight.setSmartCurrentLimit(25);
            hopperSparkRight.setInverted(true);
            hopperSparkLeft.setCANTimeout(RobotConstants.CAN_TIMEOUT);
            hopperSparkRight.setCANTimeout(RobotConstants.CAN_TIMEOUT);
            hopperSparkLeft.burnFlash();
            hopperSparkRight.burnFlash();
        }

        hopperFWDSpeed = new Calibration("Hopper Forward Speed", 0.50, 0, 1);
        hopperBWDSpeed = new Calibration("Hopper Backwards Speed", -0.75, -1, 0);
    }

    public void update(){
        if(hopperOpMode==HopperOpMode.Stop){
            HopperSparkLeftCmd=0;
            HopperSparkRightCmd=0;
        }else if(hopperOpMode==HopperOpMode.Injest){
            if(conv.getLowerSensorValue()){
                HopperSparkLeftCmd=0;
                HopperSparkRightCmd=0;
            }else{
                HopperSparkLeftCmd=hopperFWDSpeed.get();
                HopperSparkRightCmd=hopperFWDSpeed.get();
            }
        }else if(hopperOpMode==HopperOpMode.ClearJam){
            clearJamMethod1();
        }else if(hopperOpMode==HopperOpMode.Reverse){
            HopperSparkLeftCmd=hopperBWDSpeed.get();
            HopperSparkRightCmd=hopperBWDSpeed.get();
        }

        if(Robot.isReal()){
            hopperSparkLeft.set(HopperSparkLeftCmd);
            hopperSparkRight.set(HopperSparkRightCmd);
        }

    }

    //Pass in the desired hopper operation mode
    public void setOpMode(HopperOpMode cmd){
        hopperOpMode=cmd;
    }

    //Various dances of the antsy jammed up hopper people.

    int counter=0;
    final int TIMER_LIMIT_LOOPS=30; //completely arbitrary number of loops to switch
    double randCmd=0;

    public void clearJamMethod1(){
        HopperSparkLeftCmd=hopperFWDSpeed.get();
        HopperSparkRightCmd=hopperBWDSpeed.get();
    }
    public void clearJamMethod2(){
        HopperSparkLeftCmd=hopperBWDSpeed.get();
        HopperSparkRightCmd=hopperFWDSpeed.get();
    }
    public void clearJamMethod3(){
        counter++;
        if (counter%TIMER_LIMIT_LOOPS==0){
            randCmd=Math.random();
        }
        HopperSparkLeftCmd=randCmd;
        HopperSparkRightCmd=-randCmd;
    }
    public void clearJamMethod4(){
        counter++;
        if (counter%TIMER_LIMIT_LOOPS==0){
            randCmd=Math.random();
        }
        HopperSparkLeftCmd=randCmd;
        HopperSparkRightCmd=randCmd;
    }

}