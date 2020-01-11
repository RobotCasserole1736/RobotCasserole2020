package frc.robot.ControlPanel;

/*
 *******************************************************************************************
 * Copyright (C) 2019 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *   find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *   you have going on right now! We'd love to be able to help out! Shoot us 
 *   any questions you may have, all our contact info should be on our website
 *   (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *   Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *   if you would consider donating to our club to help further STEM education.
 */
import edu.wpi.first.wpilibj.I2C;
 import edu.wpi.first.wpilibj.util.Color;
  import com.revrobotics.ColorSensorV3;
   import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
    import com.revrobotics.ColorMatchResult;
	 import com.revrobotics.ColorMatch;
	  import frc.lib.DataServer.Signal;
	   import edu.wpi.first.wpilibj.Timer;
	    import frc.robot.LoopTiming;
 public class CasseroleColorSensor {
	private static CasseroleColorSensor instance = null;

	public static synchronized CasseroleColorSensor getInstance() {
		if(instance == null)
		instance = new CasseroleColorSensor();
		return instance;
	}

	ColorSensorV3 m_colorSensor;
	I2C.Port i2cPort;
	ColorMatch m_colorMatcher;
	Color kBlueTarget;
	Color kGreenTarget;
	Color kRedTarget;
	Color kYellowTarget;
	Signal RedColorValueSig;
	Signal GreenColorValueSig;
	Signal BlueColorValueSig;
	Signal ConfidenceValueSig;
	double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private CasseroleColorSensor() {
		i2cPort = I2C.Port.kOnboard;
		m_colorSensor = new ColorSensorV3(i2cPort);
		m_colorMatcher = new ColorMatch();
		kBlueTarget = ColorMatch.makeColor(0.143, 0.427, 0.429);
		kGreenTarget = ColorMatch.makeColor(0.197, 0.561, 0.240);
		kRedTarget = ColorMatch.makeColor(0.561, 0.232, 0.114);
		kYellowTarget = ColorMatch.makeColor(0.361, 0.524, 0.113);
		m_colorMatcher.addColorMatch(kBlueTarget);
		m_colorMatcher.addColorMatch(kGreenTarget);
	 	m_colorMatcher.addColorMatch(kRedTarget);
		m_colorMatcher.addColorMatch(kYellowTarget);

		//Signals
		

		RedColorValueSig = new Signal("ColorSensor", "red");
		GreenColorValueSig = new Signal("ColorSensor", "green");
		BlueColorValueSig = new Signal("ColorSensor", "blue");
		ConfidenceValueSig = new Signal("ColorSensor", "confidence");
		//TODO - Put code to init the sensor & its processing
	}

	double IR;
	String colorString;
	Color detectedColor; 

	// This method should be called once per loop.
	// It will sample the values from the sensor, and calculate what color it thinks things are
	public void update(){
        Timer updateTimer = new Timer();
        updateTimer.start();
		detectedColor = m_colorSensor.getColor();
		ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);
		SmartDashboard.putNumber("Red", detectedColor.red);
		SmartDashboard.putNumber("Green", detectedColor.green);
		SmartDashboard.putNumber("Blue", detectedColor.blue);
		SmartDashboard.putNumber("Confidence", match.confidence);
		//TODO - fill me out
		RedColorValueSig.addSample(sampleTimeMS, detectedColor.red);	
		GreenColorValueSig.addSample(sampleTimeMS, detectedColor.green);	
		BlueColorValueSig.addSample(sampleTimeMS, detectedColor.blue);	
		ConfidenceValueSig.addSample(sampleTimeMS, match.confidence);	
	}


	//Returns the best-guess color seen by the sensor.
	public ControlPanelColor getColor(){
		ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);
		if (match.color == kBlueTarget) {
		return ControlPanelColor.kBLUE;
		} else if (match.color == kRedTarget) {
		return ControlPanelColor.kRED;
		} else if (match.color == kGreenTarget) {
		return ControlPanelColor.kGREEN;
		} else if (match.color == kYellowTarget) {
		return ControlPanelColor.kYELLOW;
		} else {
		return ControlPanelColor.kUNKNOWN;
		}
	 //TODO - make this return something useful
	}

	//Get the confidence percentage as to how certain the sensor is of the color seen.
	public double getConfidence(){
		ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);
		return match.confidence * 100; //TODO - make this return something useful
	}


}