package frc.robot.BallHandling;

import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import frc.robot.CasserolePDP;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import edu.wpi.first.hal.sim.mockdata.PDPDataJNI;




public class Conveyor{

    Spark conveyorMotor;
    DigitalInput shooterEndSensor;
    BallCounter intakeEndSensor;
    PDPDataJNI pdp;
    Encoder conveyorEncoder;
    
    double conveyorPosition;

    //One revolution is equal to 8192 counts on the encoder.
    int encoderCountsInOneRevolution = 8192;
    
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
    Signal conveyorPositionSig;
    
    
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
        shooterEndSensor = new DigitalInput(RobotConstants.CONVEYOR_TO_SHOOTER_SENSOR_DIO_PORT);
        conveyorEncoder = new Encoder(RobotConstants.CONVEYOR_ENCODER_A_DIO_PORT, RobotConstants.CONVEYOR_ENCODER_B_DIO_PORT);
        intakeEndSensor = BallCounter.getInstance();
        
        //Calibrations
        conveyorLoadingSpeedCal = new Calibration("Default Calibration for Loading from Hopper to Conveyor", 0.5);
        conveyorPrepToShootCal = new Calibration("Operator Says Stop Loading and Shoot", 0.5);
        conveyorFullSendCal = new Calibration("Full Send", 0.85);
        conveyorReverseCal = new Calibration("EmptyTheRobot", 0.6);

        convMotorSpeedCmdSig = new Signal("Conveyor Motor Speed Command", "pct");
        motorCurrentSig = new Signal("Conveyor Motor Current", "A");
        shooterEndSensorSig = new Signal("Conveyor Shooter End Ball Present", "bool");
        intakeEndSensorSig = new Signal("Conveyor Intake End Ball Present", "bool");
        conveyorPositionSig = new Signal("Conveyor Position", "feet");
    }

    public void sampleSensors() {
        intakeEndSensor.update();
        intakeEndSensorTriggered = intakeEndSensor.isBallPresent();
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

        //The conveyor's position is the number of revolutions of the encoder multiplied by pi (diameter is 1 so circumference is equal to pi), 
        //and the ratio of the belt from the motor to the driving shaft of the roller. The multiplication of 12 is to make it in feet.
        conveyorPosition = conveyorEncoder.get()*encoderCountsInOneRevolution*Math.PI*RobotConstants.CONVEYOR_BELT_RATIO*12;
        
        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec()*1000;
        convMotorSpeedCmdSig.addSample(sampleTimeMS, conveyorMotor.getSpeed()); 
        motorCurrentSig.addSample(sampleTimeMS, motorCurrent);
        shooterEndSensorSig.addSample(sampleTimeMS, shooterEndSensorTriggered);
        intakeEndSensorSig.addSample(sampleTimeMS, intakeEndSensorTriggered);
        conveyorPositionSig.addSample(sampleTimeMS, conveyorPosition);
    
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