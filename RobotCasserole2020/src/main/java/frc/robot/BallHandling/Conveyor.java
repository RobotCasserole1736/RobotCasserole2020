package frc.robot.BallHandling;

public class Conveyor{

    /* Singleton infratructure*/
    private static Conveyor inst = null;
    public static synchronized Conveyor getInstance() {
        if (inst == null)
            inst = new Conveyor();
        return inst;
    }


    /* All possible conveyer operational modes*/
    public enum ConveyerOpMode {
        Stop(0),               //No Motion
        AdvanceFromHopper(1),  //Pull any available ball in from the hopper, no motion otherwise. Don't push any balls past the shooter sensor.
        AdvanceToShooter(2),   //Run forward until the conveyor->shooter sensor sees the first ball, but no further.
        InjectIntoSHooter(3),  //Run forward continuously, pushing balls up into the shooter wheel
        Reverse(4);            //Run balls back toward the hopper unconditionally

        public final int value;

        private ConveyerOpMode(int value) {
            this.value = value;
        }
    }



    private Conveyor(){
        //TODO
    }

    public void update(){
        //TODO
    }

    // Pass in the desired conveyer operational mode
    public void setOpMode(ConveyerOpMode cmd){
        //TODO
    }

    public boolean getLowerSensorValue(){
        //TODO - return true if the hopper->conveyor sensor sees a ball, false otherwise
        return false;
    }

    public boolean getUpperSensorValue(){
        //TODO - return true if the conveyor->shooter sensor sees a ball, false otherwise
        return false;
    }

}