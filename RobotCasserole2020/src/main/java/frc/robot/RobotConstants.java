package frc.robot;

import com.revrobotics.ColorMatch;

import edu.wpi.first.wpilibj.util.Color;

public class RobotConstants {


    //This class should contain a bunch of static-public-final constant values
    // which other classes can reference. Use it for things that would otherwise
    // be hardcoded, to give your values meaningful names.

    static public final int DT_LEFT_NEO_1_CANID = 1;
    static public final int DT_LEFT_NEO_2_CANID = 2;
    static public final int DT_RIGHT_NEO_1_CANID = 3;
    static public final int DT_RIGHT_NEO_2_CANID = 4;
    static public final int SHOOTER_MOTOR_1 = 5;
    static public final int SHOOTER_MOTOR_2 = 6;

    public static final int PNEUMATICS_CONTROL_MODULE_CANID = 0;
    public static final int ANALOG_PRESSURE_SENSOR_PORT = 1;

    // Robot Physical Parameters
	public static final double WHEEL_RADIUS_FT = 0.25;
	public static final double ROBOT_TRACK_WIDTH_FT = 1.75;
    public static final double MAIN_LOOP_SAMPLE_RATE_S = 0.02;
    
    //Color Sensor Configuration
    public static final Color kBlueTarget   = ColorMatch.makeColor(0.150, 0.419, 0.431);
	public static final Color kGreenTarget  = ColorMatch.makeColor(0.268, 0.498, 0.234);
	public static final Color kRedTarget    = ColorMatch.makeColor(0.470, 0.368, 0.161);
	public static final Color kYellowTarget = ColorMatch.makeColor(0.376, 0.423, 0.202);

}