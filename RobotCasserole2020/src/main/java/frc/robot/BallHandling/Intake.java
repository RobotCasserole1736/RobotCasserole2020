package frc.robot.BallHandling;

public class Intake{

    /* Singleton infratructure*/
    private static Intake inst = null;
    public static synchronized Intake getInstance() {
        if (inst == null)
            inst = new Intake();
        return inst;
    }

    /* All possible intake operational modes*/
    public enum IntakeOpMode {
        Stop(0),    // No Motion
        Injest(1),  // Intake should pull balls from the floor into the hopper
        Puke(2);    // Intake should eject balls back onto the field

        public final int value;

        private IntakeOpMode(int value) {
            this.value = value;
        }
    }


    private Intake(){

    }

    public void update(){
        //TODO
    }

    //Pass true to retract the intake into the robot, false to deploy it outside the frame perimiter
    public void setRetractCmd(boolean cmd){
        //TODO  
    }

    //Pass which state the intake should run in. 
    public void setSpeedCmd(IntakeOpMode cmd){
        //TODO
    }


}