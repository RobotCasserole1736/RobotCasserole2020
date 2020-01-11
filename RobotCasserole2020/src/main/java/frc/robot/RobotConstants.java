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

    // Robot Physical Parameters
	public static final double WHEEL_RADIUS_FT = 0.25;
	public static final double ROBOT_TRACK_WIDTH_FT = 1.75;
    public static final double MAIN_LOOP_SAMPLE_RATE_S = 0.02;
    
    //Color Sensor Configuration
    public static final Color kBlueTarget   = ColorMatch.makeColor(0.143, 0.427, 0.429);
	public static final Color kGreenTarget  = ColorMatch.makeColor(0.197, 0.561, 0.240);
	public static final Color kRedTarget    = ColorMatch.makeColor(0.561, 0.232, 0.114);
	public static final Color kYellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);

}