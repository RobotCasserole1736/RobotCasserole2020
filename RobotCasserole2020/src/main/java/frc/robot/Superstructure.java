package frc.robot;

import frc.robot.BallHandling.Conveyor;
import frc.robot.BallHandling.Hopper;
import frc.robot.BallHandling.IntakeControl;
import frc.robot.BallHandling.Conveyor.ConveyerOpMode;
import frc.robot.BallHandling.Hopper.HopperOpMode;
import frc.robot.BallHandling.IntakeControl.IntakePosition;
import frc.robot.BallHandling.IntakeControl.IntakeSpeed;
import frc.robot.ShooterControl.ShooterControl;
import frc.robot.ShooterControl.ShooterControl.ShooterCtrlMode;
import frc.robot.ShooterControl.ShooterControl.ShooterRunCommand;

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
        Normal(0),               //No intake, but advance balls from the hopper into the conveyor
        PrepToShoot(1),          //Run shooter wheel up to speed, and advance any balls in the system as close to the shooter as possible
        Shoot(2),                //Run all balls in the system through the shooter.
        Intake(3),               //Same as normal, but with the addition of the intake sucking from the ground.
        Eject(4),                //Eject everything as quickly as possible from every subsystem
        PrepShootWhileIntake(5), //Run intake while getting shooter wheel up to speed and advancing balls
        Stop(6),                 //Estop - everything is off
        ClearJam(7);             //When balls get stuck

        public final int value;

        private SuperstructureOpMode(int value) {
            this.value = value;
        }
    }

    Conveyor conv;
    Hopper   hopp;
    IntakeControl   intk;
    ShooterControl shoot;

    boolean shootDes;
    boolean prepShootDes;
    boolean intkDes;
    boolean ejectDes;
    boolean stopDes;
    boolean clearJamDes;

    SuperstructureOpMode opMode = SuperstructureOpMode.Normal; //Default

    public Superstructure(){
        conv =  Conveyor.getInstance();
        hopp =  Hopper.getInstance();
        intk =  IntakeControl.getInstance();
        shoot = ShooterControl.getInstance();
    }

    public void update(){
        conv.update();
        hopp.update();
        intk.update();
        shoot.update();

        if(stopDes){
            opMode = SuperstructureOpMode.Stop;
        }else if(ejectDes && !stopDes){
            opMode = SuperstructureOpMode.Eject;
        }else if(clearJamDes && !stopDes && !ejectDes){
            opMode = SuperstructureOpMode.ClearJam;
        }else if(shootDes && !stopDes && !prepShootDes && !ejectDes && !clearJamDes){
            if(shoot.getShooterCtrlMode() == ShooterCtrlMode.HoldSpeed){
                opMode = SuperstructureOpMode.Shoot;
            }else if(shoot.getShooterCtrlMode() != ShooterCtrlMode.HoldSpeed){
                opMode = SuperstructureOpMode.PrepToShoot;
            } 
        }else if(prepShootDes && intkDes && !stopDes && !ejectDes && !clearJamDes){
            opMode = SuperstructureOpMode.PrepShootWhileIntake;
        }else if(prepShootDes && !stopDes && !intkDes && !ejectDes && !clearJamDes){
            opMode = SuperstructureOpMode.PrepToShoot;
        }else if(intkDes && !stopDes && !shootDes && !prepShootDes && !ejectDes && !clearJamDes){
            opMode = SuperstructureOpMode.Intake;
        }else{ //if(!intkDes && !stopDes && !shootDes && !prepShootDes && !ejectDes && !clearJamDes)
            opMode = SuperstructureOpMode.Normal;
        }

        setOpMode(opMode);
    }

    private void setOpMode(SuperstructureOpMode opMode_in){
        switch(opMode_in){
            case Normal:
            intk.setPosMode(IntakePosition.Retracted);
            intk.setSpeedMode(IntakeSpeed.Stop);
            hopp.setOpMode(HopperOpMode.Injest);
            shoot.setRun(ShooterRunCommand.Stop);
            conv.setOpMode(ConveyerOpMode.AdvanceFromHopper);
            break;
            case PrepToShoot:
            intk.setSpeedMode(IntakeSpeed.Stop);
            hopp.setOpMode(HopperOpMode.Injest);
            shoot.setRun(ShooterRunCommand.ShotFar);
            conv.setOpMode(ConveyerOpMode.AdvanceToShooter);
            break;
            case Shoot:
            intk.setSpeedMode(IntakeSpeed.Stop);
            hopp.setOpMode(HopperOpMode.Injest);
            shoot.setRun(ShooterRunCommand.ShotFar);
            conv.setOpMode(ConveyerOpMode.InjectIntoShooter);
            break;
            case Intake:
            intk.setPosMode(IntakePosition.Extended);
            intk.setSpeedMode(IntakeSpeed.Intake);
            hopp.setOpMode(HopperOpMode.Injest);
            conv.setOpMode(ConveyerOpMode.AdvanceFromHopper);
            shoot.setRun(ShooterRunCommand.Stop);
            break;
            case Eject:
            shoot.setRun(ShooterRunCommand.Stop);
            conv.setOpMode(ConveyerOpMode.Reverse);
            hopp.setOpMode(HopperOpMode.Reverse);
            intk.setPosMode(IntakePosition.Extended);
            intk.setSpeedMode(IntakeSpeed.Eject);
            break;
            case Stop:
            intk.setSpeedMode(IntakeSpeed.Stop);
            hopp.setOpMode(HopperOpMode.Stop);
            conv.setOpMode(ConveyerOpMode.Stop);
            shoot.setRun(ShooterRunCommand.Stop);
            break;
            case ClearJam:
            intk.setSpeedMode(IntakeSpeed.Stop);
            hopp.setOpMode(HopperOpMode.ClearJam);
            conv.setOpMode(ConveyerOpMode.Stop);
            break;
        }
    }

    //Set to true when the operator or some auto routine wants to be shooting balls, false otherwise.
    public void setShootDesired(boolean cmd){
        shootDes = cmd;
    }

    //This should do the 1a/b/c actions above, but not advance to the 2a/b actions.
    public void setPrepToShootDesired(boolean cmd){
        prepShootDes = cmd;
    }

    public void setIntakeDesired(boolean cmd){
        intkDes = cmd;
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


    

}