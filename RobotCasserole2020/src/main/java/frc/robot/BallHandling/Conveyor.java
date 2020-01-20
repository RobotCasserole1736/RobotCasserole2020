package frc.robot.BallHandling;

import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.RobotConstants;
import frc.lib.Calibration.Calibration;



public class Conveyor{

    CANSparkMax conveyorMotor;
    DigitalInput shooterEndSensor;
    DigitalInput intakeEndSensor;
    
    //Calibrations
    Calibration conveyorLoadingSpeedCal;
    Calibration conveyorshootNowSpeedCal;

    //State Data
    boolean shooterEndSensorTriggered = false;
    boolean intakeEndSensorTriggered = false;


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
        conveyorMotor = new CANSparkMax(RobotConstants.CONVEYOR_MOTOR, MotorType.kBrushless);
        shooterEndSensor = new DigitalInput(RobotConstants.CONVEYOR_TO_SHOOTER_DIO_PORT);
        intakeEndSensor = new DigitalInput(RobotConstants.CONVEYOR_TO_INTAKE_DIO_PORT);

        //Calibrations
        conveyorLoadingSpeedCal = new Calibration("Default Calibration for Loading from Hopper to Conveyor", 0.3);
        conveyorshootNowSpeedCal = new Calibration("Operator Says Stop Loading and Shoot", 0.5);

    }
    public void sampleSensors() {
        intakeEndSensorTriggered = intakeEndSensor.get();
        shooterEndSensorTriggered = shooterEndSensor.get();

    }

    public void update(){
        sampleSensors();
        
    }

    // Pass in the desired conveyer operational mode
    // AdvanceFromHopper(1),  //Pull any available ball in from the hopper, no motion otherwise. Don't push any balls past the shooter sensor.
    //     AdvanceToShooter(2),   //Run forward until the conveyor->shooter sensor sees the first ball, but no further.
    //     InjectIntoSHooter(3),  //Run forward continuously, pushing balls up into the shooter wheel
    //     Reverse(4); 
    public void setOpMode(ConveyerOpMode cmd){
        switch(cmd) {
            case Stop:
            conveyorMotor.set(0);
            case AdvanceFromHopper:
            conveyorMotor.set(conveyorLoadingSpeedCal);
            case InjectIntoShooter:
            ;
            case Reverse:
            ;

        }
    }

    

    public boolean getLowerSensorValue(){
        //TODO - return true if the hopper->conveyor sensor sees a ball, false otherwise
        return intakeEndSensorTriggered;
    }

    public boolean getUpperSensorValue(){
        //TODO - return true if the conveyor->shooter sensor sees a ball, false otherwise
        return shooterEndSensorTriggered;
    }

}