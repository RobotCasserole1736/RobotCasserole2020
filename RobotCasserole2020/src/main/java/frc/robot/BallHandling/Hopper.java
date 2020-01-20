package frc.robot.BallHandling;
import edu.wpi.first.wpilibj.Spark;
import frc.robot.RobotConstants;
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
    Spark HopperSpark1;
    Spark HopperSpark2;
    int counter=0;
    int timer=30; //completely arbitrary number of loops to switch
    double randCmd=0;
    double HopperSpark1Cmd=0;
    double HopperSpark2Cmd=0;
    Signal HopperSpark1CurrentSignal;
    Signal HopperSpark2CurrentSignal;
    Signal HopperSpark1CmdSignal;
    Signal HopperSpark2CmdSignal;

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
        HopperSpark1= new Spark(RobotConstants.HOPPER_SPARK_1_ID);
        HopperSpark2= new Spark(RobotConstants.HOPPER_SPARK_2_ID);
        HopperSpark1CurrentSignal =new Signal("Hopper Spark 1 Current","Amp");
        HopperSpark2CurrentSignal =new Signal("Hopper Spark 2 Current","Amp");
        HopperSpark1CmdSignal =new Signal("Hopper Spark 1 Cmd","Cmd");
        HopperSpark2CmdSignal =new Signal("Hopper Spark 2 Cmd","Cmd");
    }

    public void update(){
        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;

        if(hopperOpMode==HopperOpMode.Stop){
            HopperSpark1Cmd=0;
            HopperSpark2Cmd=0;
        }else if(hopperOpMode==HopperOpMode.Injest){
            HopperSpark1Cmd=1;
            HopperSpark2Cmd=1;
        }else if(hopperOpMode==HopperOpMode.ClearJam){
            //TODO test this
            counter++;
            clearJamMethod1();
        }else if(hopperOpMode==HopperOpMode.Reverse){
            HopperSpark1Cmd=-1;
            HopperSpark2Cmd=-1;
        }
        HopperSpark1.set(HopperSpark1Cmd);
        HopperSpark2.set(HopperSpark2Cmd);
        HopperSpark1CmdSignal.addSample(sampleTimeMs, HopperSpark1Cmd);
        HopperSpark2CmdSignal.addSample(sampleTimeMs, HopperSpark2Cmd);
        HopperSpark1CurrentSignal.addSample(sampleTimeMs, CasserolePDP.getInstance().getCurrent(RobotConstants.HOPPER_SPARK_1_PDP_ID));
        HopperSpark2CurrentSignal.addSample(sampleTimeMs, CasserolePDP.getInstance().getCurrent(RobotConstants.HOPPER_SPARK_2_PDP_ID));
    }

    //Pass in the desired hopper operation mode
    public void setOpMode(HopperOpMode cmd){
        hopperOpMode=cmd;
    }
    public void clearJamMethod1(){
        HopperSpark1Cmd=1;
        HopperSpark2Cmd=-1;
    }
    public void clearJamMethod2(){
        HopperSpark1Cmd=-1;
        HopperSpark2Cmd=1;
    }
    public void clearJamMethod3(){
        if (counter%timer==0){
            randCmd=Math.random();
        }
        HopperSpark1Cmd=randCmd;
        HopperSpark2Cmd=-randCmd;
    }
    public void clearJamMethod4(){
        if (counter%timer==0){
            randCmd=Math.random();
        }
        HopperSpark1Cmd=randCmd;
        HopperSpark2Cmd=randCmd;
    }

}