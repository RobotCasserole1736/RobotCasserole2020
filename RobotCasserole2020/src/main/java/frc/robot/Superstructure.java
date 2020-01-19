package frc.robot;

import frc.robot.BallHandling.Conveyor;
import frc.robot.BallHandling.Hopper;
import frc.robot.BallHandling.Intake;

public class Superstructure {

    /* Singleton infratructure */
    private static Superstructure inst = null;

    public static synchronized Superstructure getInstance() {
        if (inst == null)
            inst = new Superstructure();
        return inst;
    }


    /* All possible conveyer operational modes*/
    public enum SuperstructureOpMode {
        Normal(0),             //No intake, but advance balls from the hopper into the conveyor
        PrepToShoot(1),        //Run shooter wheel up to speed, and advance any balls in the system as close to the shooter as possible
        Shoot(2),              //Run all balls in the system through the shooter.
        Intake(3),             //Same as normal, but with the addition of the intake sucking from the ground.
        Eject(4);              //Eject everything as quickly as possible from every subsystem

        public final int value;

        private SuperstructureOpMode(int value) {
            this.value = value;
        }
    }

    Conveyor conv;
    Hopper   hopp;
    Intake   intk;

    public Superstructure(){
        conv =  Conveyor.getInstance();
        hopp =  Hopper.getInstance();
        intk =  Intake.getInstance();
    }

    public void update(){

        //TODO - main logic to configure each individual subsystem, based on Superstructure commands

        conv.update();
        hopp.update();
        intk.update();
    }

    //Set to true when the operator or some auto routine wants to be shooting balls, false otherwise.
    public void setShootDesired(boolean cmd){
        //TODO
    }

    //This should do the 1a/b/c actions above, but not advance to the 2a/b actions.
    public void setPrepToShootDesired(boolean cmd){
        //TODO
    }

    public void setIntakeDesired(boolean cmd){
        //TODO
    }

    public void setEjectDesired(boolean cmd){
        //TODO
    }

    //This should cause all mechanisms to stop moving, if set to true. Allow normal operation if false.
    public void setEstopDesired(boolean cmd){
        //TODO
    }


    

}