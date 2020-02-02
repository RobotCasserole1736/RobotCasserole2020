package frc.robot.BallHandling;

import com.revrobotics.CANSparkMax;
import edu.wpi.first.wpilibj.Spark;


import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.CasserolePDP;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;
import frc.robot.BallHandling.BallCounter.ConveyorDirection;
import frc.robot.Supperstructure.SupperstructureOpMode;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import edu.wpi.first.hal.PDPJNI;
import edu.wpi.first.hal.sim.mockdata.PDPDataJNI;




public class Conveyor{

    Spark conveyorMotor;
    DigitalInput shooterEndSensor;
    DigitalInput intakeEndSensor;
    PDPDataJNI pdp;
    
    
    //Calibrations
    Calibration conveyorLoadingSpeedCal;
    Calibration conveyorPrepToShootCal;
    Calibration conveyorFullSendCal;
    Calibration conveyorReverseCal;


    //State Data
    ConveyerOpMode opMode = ConveyerOpMode.Stop;
    ConveyerOpMode prevOpMode = ConveyerOpMode.Stop;
    boolean shooterEndSensorTriggered = false;
    boolean intakeEndSensorTriggered = false;
    double motorCurrent;
    
    Signal convMotorSpeedCmdSig; 
    Signal motorCurrentSig;
    Signal shooterEndSensorSig;
    Signal intakeEndSensorSig;
    
    
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
        InjectIntoShooter(3),  //Run forward continuously, pushing balls up into the shooter wheel
        Reverse(4);            //Run balls back toward the hopper unconditionally

        public final int value;

        private ConveyerOpMode(int value) {
            this.value = value;
        }
    }



    private Conveyor(){
        //Physical Devices
        conveyorMotor = new Spark(RobotConstants.CONVEYOR_MOTOR);
        shooterEndSensor = new DigitalInput(RobotConstants.CONVEYOR_TO_SHOOTER_DIO_PORT);
        intakeEndSensor = new DigitalInput(RobotConstants.CONVEYOR_TO_INTAKE_DIO_PORT);

        //Calibrations
        conveyorLoadingSpeedCal = new Calibration("Default Calibration for Loading from Hopper to Conveyor", 0.5);
        conveyorPrepToShootCal = new Calibration("Operator Says Stop Loading and Shoot", 0.5);
        conveyorFullSendCal = new Calibration("Full Send", 0.85);
        conveyorReverseCal = new Calibration("EmptyTheRobot", 0.6);

        convMotorSpeedCmdSig = new Signal("Speed Command for the Conveyor Motor", "%");
        motorCurrentSig = new Signal("Conveyor Motor Current", "Amps");
        shooterEndSensorSig = new Signal("Is there a Ball at the Shooter End Conveyor", "Boolean");
        intakeEndSensorSig = new Signal("Is there a ball at the Intake End of the Conveyor", "Boolean");
    }

    public void sampleSensors() {
        intakeEndSensorTriggered = intakeEndSensor.get();
        shooterEndSensorTriggered = shooterEndSensor.get();
        
        motorCurrent = CasserolePDP.getInstance().getCurrent(RobotConstants.CONVEYOR_MOTOR_PDP_CHANNEL);
    }

    public void update(){
        sampleSensors();

        switch(opMode) {
            case Stop:
                conveyorMotor.set(0);
            break;

            case AdvanceFromHopper:
                if(intakeEndSensorTriggered) {
                    conveyorMotor.set(conveyorLoadingSpeedCal.get());
                    
                }else{
                    conveyorMotor.set(0);   
                }    
            break;

            case AdvanceToShooter:
                if(!shooterEndSensorTriggered) {
                    conveyorMotor.set(conveyorPrepToShootCal.get());
                }else{
                    conveyorMotor.set(0);
                }
            break;

            case InjectIntoShooter:
                conveyorMotor.set(conveyorFullSendCal.get());
            break;
        
            case Reverse:
                conveyorMotor.set(conveyorReverseCal.get());
            break;
        }
        prevOpMode = opMode;
        
        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec()*1000;
        convMotorSpeedCmdSig.addSample(sampleTimeMS, conveyorMotor.getSpeed()); 
        motorCurrentSig.addSample(sampleTimeMS, motorCurrent);
        shooterEndSensorSig.addSample(sampleTimeMS, shooterEndSensorTriggered);
        intakeEndSensorSig.addSample(sampleTimeMS, intakeEndSensorTriggered);

    
    }
    public void setOpMode(ConveyerOpMode opMode_in) {
        opMode = opMode_in;
    }
    

    // Pass in the desired conveyer operational mode
    // AdvanceFromHopper(1),  //Pull any available ball in from the hopper, no motion otherwise. Don't push any balls past the shooter sensor.
    //     AdvanceToShooter(2),   //Run forward until the conveyor->shooter sensor sees the first ball, but no further.
    //     InjectIntoSHooter(3),  //Run forward continuously, pushing balls up into the shooter wheel
    //     Reverse(4); 

    public boolean getLowerSensorValue(){
        // return true if the hopper->conveyor sensor sees a ball, false otherwise
        return intakeEndSensorTriggered;
    }

    public boolean getUpperSensorValue(){
        // return true if the conveyor->shooter sensor sees a ball, false otherwise
        return shooterEndSensorTriggered;
    }
    public ConveyerOpMode getOpMode() {
        return opMode;
    }

}