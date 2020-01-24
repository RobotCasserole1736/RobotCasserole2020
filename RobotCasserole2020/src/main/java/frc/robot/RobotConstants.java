package frc.robot;

public class RobotConstants {


    //This class should contain a bunch of static-public-final constant values
    // which other classes can reference. Use it for things that would otherwise
    // be hardcoded, to give your values meaningful names.

    //CAN ID's
    static public final int DT_LEFT_NEO_1_CANID = 10;
    static public final int DT_LEFT_NEO_2_CANID = 11;
    static public final int DT_RIGHT_NEO_1_CANID = 12;
    static public final int DT_RIGHT_NEO_2_CANID = 13;
    static public final int SHOOTER_MOTOR_1 = 14;
    static public final int SHOOTER_MOTOR_2 = 15;
    public static final int POWER_DISTRIBUTION_PANEL_CANID = 0; 
    public static final int PNEUMATICS_CONTROL_MODULE_CANID = 0;

    //PWM Outputs
    public static final int CONVEYOR_MOTOR = 0;
    static public final int INTAKE_MOTOR = 1;
    static public final int HOPPER_SPARK_LEFT_ID=2;
    static public final int HOPPER_SPARK_RIGHT_ID=3;
    static public final int CLIMBER_SPARK_LEFT_ID=4;
    static public final int CLIMBER_SPARK_RIGHT_ID=5;

    //PDP Current Measurements 
    static public final int HOPPER_SPARK_LEFT_PDP_ID = 0;
    static public final int HOPPER_SPARK_RIGHT_PDP_ID = 1;
    static public final int CLIMBER_SPARK_LEFT_PDP_ID=2;
    static public final int CLIMBER_SPARK_RIGHT_PDP_ID=3;
    public static final int CONVEYOR_MOTOR_PDP_CHANNEL = 4;

    //Digital IO
    static public final int VISON_LED_RING_PORT = 0;
    static public final int PHOTON_CANNON_PORT = 1;
    static public final int CLIMBER_LIMIT_UPPER_ID=2;
    static public final int CLIMBER_LIMIT_LOWER_ID=3;
    public static final int CONVEYOR_TO_SHOOTER_DIO_PORT = 4;
    public static final int CONVEYOR_TO_INTAKE_DIO_PORT = 5;

    //Pneumatics Control Module
    static public final int CLIMBER_SOLENOID_ID = 0;
    static public final int INTAKE_SOLENOID_FWD = 1; 
    static public final int INTAKE_SOLENOID_REV = 2;

    //Analog Ports
    public static final int ANALOG_PRESSURE_SENSOR_PORT = 0;

    // Robot Physical Parameters
	public static final double WHEEL_RADIUS_FT = 3.0/12.0;
	public static final double ROBOT_TRACK_WIDTH_FT = 23.0/12.0;
    public static final double MAIN_LOOP_SAMPLE_RATE_S = 0.02;

    public static final double SHOOTER_GEAR_RATIO = 1.0;
    public static final double DRIVETRAIN_GEAR_RATIO = 15.0; //TODO - is this right?
    
    //Color Sensor Configuration
    
}