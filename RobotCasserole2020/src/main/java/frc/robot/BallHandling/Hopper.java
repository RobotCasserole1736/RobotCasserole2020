package frc.robot.BallHandling;
import edu.wpi.first.wpilibj.Spark;
import frc.robot.RobotConstants;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import frc.robot.CasserolePDP;

public class Hopper{

    /* Singleton infratructure*/
    private static Hopper inst = null;
    public static synchronized Hopper getInstance() {
        if (inst == null)
            inst = new Hopper();
        return inst;
    }
    HopperOpMode hopperOpMode;
    Spark HopperSparkLeft;
    Spark HopperSparkRight;
    int counter=0;
    int timer=30; //completely arbitrary number of loops to switch
    double randCmd=0;
    double HopperSparkLeftCmd=0;
    double HopperSparkRightCmd=0;
    Signal HopperSparkLeftCurrentSignal;
    Signal HopperSparkRightCurrentSignal;
    Signal HopperSparkLeftCmdSignal;
    Signal HopperSparkRightCmdSignal;
    Calibration HopperFWDSpeed;
    Calibration HopperBWDSpeed;

    /* All possible intake speed commands*/
    public enum HopperOpMode {
        Stop(0),     //No Motion
        Injest(1),   //Run balls toward the conveyer
        ClearJam(2), //Randomly (ish) change direction in an attempt to un-jam stuck balls
        Reverse(1);  //Run balls toward the intake

        public final int value;

        private HopperOpMode(int value) {
            this.value = value;
        }
    }


    private Hopper(){
        HopperSparkLeft= new Spark(RobotConstants.HOPPER_SPARK_LEFT_ID);
        HopperSparkRight= new Spark(RobotConstants.HOPPER_SPARK_RIGHT_ID);
        HopperFWDSpeed = new Calibration("Hopper Forward Speed", 1);
        HopperBWDSpeed = new Calibration("Hopper Backwards Speed", -1);
        HopperSparkLeftCurrentSignal =new Signal("Hopper Spark Left Current","Amp");
        HopperSparkRightCurrentSignal =new Signal("Hopper Spark Right Current","Amp");
        HopperSparkLeftCmdSignal =new Signal("Hopper Spark Left Cmd","Cmd");
        HopperSparkRightCmdSignal =new Signal("Hopper Spark Right Cmd","Cmd");
    }

    public void update(){
        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;

        if(hopperOpMode==HopperOpMode.Stop){
            HopperSparkLeftCmd=0;
            HopperSparkRightCmd=0;
        }else if(hopperOpMode==HopperOpMode.Injest){
            HopperSparkLeftCmd=HopperFWDSpeed.get();
            HopperSparkRightCmd=HopperFWDSpeed.get();
        }else if(hopperOpMode==HopperOpMode.ClearJam){
            //TODO test this
            counter++;
            clearJamMethod1();
        }else if(hopperOpMode==HopperOpMode.Reverse){
            HopperSparkLeftCmd=HopperBWDSpeed.get();
            HopperSparkRightCmd=HopperBWDSpeed.get();
        }
        HopperSparkLeft.set(HopperSparkLeftCmd);
        HopperSparkRight.set(HopperSparkRightCmd);
        HopperSparkLeftCmdSignal.addSample(sampleTimeMs, HopperSparkLeftCmd);
        HopperSparkRightCmdSignal.addSample(sampleTimeMs, HopperSparkRightCmd);
        HopperSparkLeftCurrentSignal.addSample(sampleTimeMs, CasserolePDP.getInstance().getCurrent(RobotConstants.HOPPER_SPARK_LEFT_PDP_ID));
        HopperSparkRightCurrentSignal.addSample(sampleTimeMs, CasserolePDP.getInstance().getCurrent(RobotConstants.HOPPER_SPARK_RIGHT_PDP_ID));
    }

    //Pass in the desired hopper operation mode
    public void setOpMode(HopperOpMode cmd){
        hopperOpMode=cmd;
    }
    public void clearJamMethod1(){
        HopperSparkLeftCmd=HopperFWDSpeed.get();
        HopperSparkRightCmd=HopperBWDSpeed.get();
    }
    public void clearJamMethod2(){
        HopperSparkLeftCmd=HopperBWDSpeed.get();
        HopperSparkRightCmd=HopperFWDSpeed.get();
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