package frc.robot.BallHandling;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import frc.robot.RobotConstants;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;

public class Hopper{

    /* Singleton infratructure*/
    private static Hopper inst = null;
    public static synchronized Hopper getInstance() {
        if (inst == null)
            inst = new Hopper();
        return inst;
    }
    HopperOpMode hopperOpMode;
    CANSparkMax hopperSparkLeft;
    CANSparkMax hopperSparkRight;
    int counter=0;
    int timer=30; //completely arbitrary number of loops to switch
    double randCmd=0;
    double HopperSparkLeftCmd=0;
    double HopperSparkRightCmd=0;
    Signal hopperSparkLeftCurrentSignal;
    Signal hopperSparkRightCurrentSignal;
    Signal hopperSparkLeftCmdSignal;
    Signal hopperSparkRightCmdSignal;
    Calibration hopperFWDSpeed;
    Calibration hopperBWDSpeed;

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
        hopperSparkLeft= new CANSparkMax(RobotConstants.HOPPER_NEO_LEFT_CAN_ID, MotorType.kBrushless);
        hopperSparkRight= new CANSparkMax(RobotConstants.HOPPER_NEO_RIGHT_CAN_ID, MotorType.kBrushless);
        hopperFWDSpeed = new Calibration("Hopper Forward Speed", 0.25, 0, 1);
        hopperBWDSpeed = new Calibration("Hopper Backwards Speed", -75, -1, 0);
        hopperSparkLeftCurrentSignal =new Signal("Hopper Motor Left Current","A");
        hopperSparkRightCurrentSignal =new Signal("Hopper Motor Right Current","A");
        hopperSparkLeftCmdSignal =new Signal("Hopper Motor Left Cmd","cmd");
        hopperSparkRightCmdSignal =new Signal("Hopper Motor Right Cmd","cmd");

        hopperSparkLeft.setSmartCurrentLimit(30);
        hopperSparkRight.setSmartCurrentLimit(30);
        
        hopperSparkRight.setInverted(true);
    }

    public void update(){
        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;

        if(hopperOpMode==HopperOpMode.Stop){
            HopperSparkLeftCmd=0;
            HopperSparkRightCmd=0;
        }else if(hopperOpMode==HopperOpMode.Injest){
            HopperSparkLeftCmd=hopperFWDSpeed.get();
            HopperSparkRightCmd=hopperFWDSpeed.get();
        }else if(hopperOpMode==HopperOpMode.ClearJam){
            //TODO test this
            counter++;
            clearJamMethod1();
        }else if(hopperOpMode==HopperOpMode.Reverse){
            HopperSparkLeftCmd=hopperBWDSpeed.get();
            HopperSparkRightCmd=hopperBWDSpeed.get();
        }
        hopperSparkLeft.set(HopperSparkLeftCmd);
        hopperSparkRight.set(HopperSparkRightCmd);

        hopperSparkLeftCmdSignal.addSample(sampleTimeMs, HopperSparkLeftCmd);
        hopperSparkRightCmdSignal.addSample(sampleTimeMs, HopperSparkRightCmd);
        hopperSparkLeftCurrentSignal.addSample(sampleTimeMs, hopperSparkLeft.getOutputCurrent());
        hopperSparkRightCurrentSignal.addSample(sampleTimeMs, hopperSparkRight.getOutputCurrent());
    }

    //Pass in the desired hopper operation mode
    public void setOpMode(HopperOpMode cmd){
        hopperOpMode=cmd;
    }
    public void clearJamMethod1(){
        HopperSparkLeftCmd=hopperFWDSpeed.get();
        HopperSparkRightCmd=hopperBWDSpeed.get();
    }
    public void clearJamMethod2(){
        HopperSparkLeftCmd=hopperBWDSpeed.get();
        HopperSparkRightCmd=hopperFWDSpeed.get();
    }
    public void clearJamMethod3(){
        if (counter%timer==0){
            randCmd=Math.random();
        }
        HopperSparkLeftCmd=randCmd;
        HopperSparkRightCmd=-randCmd;
    }
    public void clearJamMethod4(){
        if (counter%timer==0){
            randCmd=Math.random();
        }
        HopperSparkLeftCmd=randCmd;
        HopperSparkRightCmd=randCmd;
    }

}