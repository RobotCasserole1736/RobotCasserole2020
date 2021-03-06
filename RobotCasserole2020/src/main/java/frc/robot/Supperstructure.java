package frc.robot;

import frc.robot.BallHandling.Conveyor;
import frc.robot.BallHandling.Hopper;
import frc.robot.BallHandling.IntakeControl;
import frc.robot.BallHandling.Conveyor.ConveyorOpMode;
import frc.robot.BallHandling.Hopper.HopperOpMode;
import frc.robot.BallHandling.IntakeControl.IntakePosition;
import frc.robot.BallHandling.IntakeControl.IntakeSpeed;
import frc.robot.ShooterControl.ShooterControl;
import frc.robot.ShooterControl.ShooterControl.ShooterCtrlMode;
import frc.robot.ShooterControl.ShooterControl.ShooterRunCommand;

public class Supperstructure {

    /* Singleton infratructure */
    private static Supperstructure inst = null;

    public static synchronized Supperstructure getInstance() {
        if (inst == null)
            inst = new Supperstructure();
        return inst;
    }


    /* All possible Conveyor operational modes*/
    public enum SupperstructureOpMode {
        Normal(0),               //No intake, but advance balls from the hopper into the conveyor
        PrepToShoot(1),          //Run shooter wheel up to speed, and advance any balls in the system as close to the shooter as possible
        ShootFar(2),             //Put balls through the shooter, waiting between each shoot for the shooter to get back to speed
        Intake(3),               //Same as normal, but with the addition of the intake sucking from the ground.
        Eject(4),                //Eject everything as quickly as possible from every subsystem
        PrepShootWhileIntake(5), //Run intake while getting shooter wheel up to speed and advancing balls
        Stop(6),                 //Estop - everything is off
        ClearJam(7),             //When balls get stuck
        ShootClose(8),           //Run all balls in the system through the shooter as fast as possible (sacrificing accuracy)
        SpinDaShooter(9),
        Repel(10);


        public final int value;

        private SupperstructureOpMode(int value) {
            this.value = value;
        }
    }

    Conveyor conv;
    Hopper   hopp;
    IntakeControl   intk;
    ShooterControl shoot;

    boolean shootFarDes;
    boolean shootCloseDes;
    boolean prepShootDes;
    boolean intkDes;
    boolean ejectDes;
    boolean stopDes;
    boolean repelDes=false;
    boolean clearJamDes;

    SupperstructureOpMode opMode = SupperstructureOpMode.Normal; //Default

    public Supperstructure(){
        conv =  Conveyor.getInstance();
        hopp =  Hopper.getInstance();
        intk =  IntakeControl.getInstance();
        shoot = ShooterControl.getInstance();
    }

    public void update(){

        ShooterCtrlMode shooterCM = shoot.getShooterCtrlMode();

        if(stopDes){
            opMode = SupperstructureOpMode.Stop;

        }else if(ejectDes){
            opMode = SupperstructureOpMode.Eject;

        }else if(clearJamDes){
            opMode = SupperstructureOpMode.ClearJam;

        }else if(shootFarDes && !prepShootDes){
            if(shooterCM == ShooterCtrlMode.HoldForShot){
                opMode = SupperstructureOpMode.ShootFar;
            } else {
                opMode = SupperstructureOpMode.PrepToShoot;
            } 

        }else if(shootCloseDes && !prepShootDes){
            if(shooterCM == ShooterCtrlMode.HoldForShot || shooterCM == ShooterCtrlMode.JustGonnaSendEm){
                opMode = SupperstructureOpMode.ShootClose;
            } else {
                opMode = SupperstructureOpMode.PrepToShoot;
            } 
            
        }else if(prepShootDes && intkDes){
            opMode = SupperstructureOpMode.PrepShootWhileIntake;

        }else if(prepShootDes && !intkDes){
            opMode = SupperstructureOpMode.PrepToShoot;

        }else if(intkDes && !shootFarDes && !prepShootDes){
            opMode = SupperstructureOpMode.Intake;

        }else{ //if(everything is false)
            opMode = SupperstructureOpMode.Normal;
        }

        setOpMode(opMode);
        
        conv.update(); 
        hopp.update();
        intk.update();
        //Shooter runs in separate thread
    }

    private void setOpMode(SupperstructureOpMode opMode_in){
        switch(opMode_in){
            case Normal:
                intk.setPosMode(IntakePosition.Retracted);
                intk.setSpeedMode(IntakeSpeed.Stop);
                hopp.setOpMode(HopperOpMode.Stop);
                shoot.setRun(ShooterRunCommand.Stop);
                conv.setOpMode(ConveyorOpMode.AdvanceFromHopper);
            break;
            case PrepToShoot:
                intk.setPosMode(IntakePosition.Retracted);
                intk.setSpeedMode(IntakeSpeed.Stop);
                hopp.setOpMode(HopperOpMode.Injest);
                shoot.setRun(ShooterRunCommand.ShotFar);
                conv.setOpMode(ConveyorOpMode.AdvanceToShooter);
            break;
            case ShootFar:
                intk.setSpeedMode(IntakeSpeed.IntakeButSlowly);
                hopp.setOpMode(HopperOpMode.Injest);
                shoot.setRun(ShooterRunCommand.ShotFar);
                conv.setOpMode(ConveyorOpMode.InjectIntoShooter);
            break;
            case ShootClose:
                intk.setSpeedMode(IntakeSpeed.IntakeButSlowly);
                hopp.setOpMode(HopperOpMode.Injest);
                shoot.setRun(ShooterRunCommand.ShotClose);
                conv.setOpMode(ConveyorOpMode.InjectIntoShooter);
            break;
            case Intake:
                intk.setPosMode(IntakePosition.Extended);
                intk.setSpeedMode(IntakeSpeed.Intake);
                hopp.setOpMode(HopperOpMode.Injest);
                conv.setOpMode(ConveyorOpMode.AdvanceFromHopper);
                shoot.setRun(ShooterRunCommand.Stop);
            break;
            case Eject:
                shoot.setRun(ShooterRunCommand.Stop);
                conv.setOpMode(ConveyorOpMode.Reverse);
                hopp.setOpMode(HopperOpMode.Reverse);
                intk.setPosMode(IntakePosition.Extended);
                intk.setSpeedMode(IntakeSpeed.Eject);
            break;
            case Stop:
                intk.setPosMode(IntakePosition.Retracted);
                intk.setSpeedMode(IntakeSpeed.Stop);
                hopp.setOpMode(HopperOpMode.Stop);
                conv.setOpMode(ConveyorOpMode.Stop);
                shoot.setRun(ShooterRunCommand.Stop);
            break;
            case ClearJam:
                intk.setPosMode(IntakePosition.Extended);
                intk.setSpeedMode(IntakeSpeed.Stop);
                hopp.setOpMode(HopperOpMode.ClearJam);
                conv.setOpMode(ConveyorOpMode.Stop);
            break;
            case PrepShootWhileIntake:
                intk.setPosMode(IntakePosition.Extended);
                intk.setSpeedMode(IntakeSpeed.Intake);
                hopp.setOpMode(HopperOpMode.Injest);
                shoot.setRun(ShooterRunCommand.ShotFar);
                conv.setOpMode(ConveyorOpMode.AdvanceFromHopper);
            break;
            case Repel:
                intk.setPosMode(IntakePosition.Extended);
                intk.setSpeedMode(IntakeSpeed.Eject);
                hopp.setOpMode(HopperOpMode.Stop);
                conv.setOpMode(ConveyorOpMode.Stop);
                shoot.setRun(ShooterRunCommand.Stop);
            break;
        }
    }

    //Set to true when the operator or some auto routine wants to be shooting balls with precision, false otherwise.
    public void setShootFarDesired(boolean cmd){
        shootFarDes = cmd;
    }

    //Set to true when the operator or some auto routine wants to be shooting balls with precision, false otherwise.
    public void setShootCloseDesired(boolean cmd){
        shootCloseDes = cmd;
    }

    //This should do the 1a/b/c actions above, but not advance to the 2a/b actions.
    public void setPrepToShootDesired(boolean cmd){
        prepShootDes = cmd;
    }

    public void setIntakeDesired(boolean cmd){
        intkDes = cmd;
    }

    public void setRepelDesired(boolean cmd){
        repelDes=cmd;
    }

    public void setEjectDesired(boolean cmd){
        ejectDes = cmd;
    }

    //This should cause all mechanisms to stop moving, if set to true. Allow normal operation if false.
    public void setEstopDesired(boolean cmd){
        stopDes = cmd;
    }

    public void setClearJamDesired(boolean cmd){
        clearJamDes = cmd;
    }

    public SupperstructureOpMode getOpMode(){
        return opMode;
    }


    

}