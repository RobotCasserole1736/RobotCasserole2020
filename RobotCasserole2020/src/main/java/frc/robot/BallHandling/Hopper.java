package frc.robot.BallHandling;

public class Hopper{

    /* Singleton infratructure*/
    private static Hopper inst = null;
    public static synchronized Hopper getInstance() {
        if (inst == null)
            inst = new Hopper();
        return inst;
    }

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

    }

    public void update(){
        //TODO
    }

    //Pass in the desired hopper operation mode
    public void setOpMode(HopperOpMode cmd){
        //TODO
    }

}