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
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;

import com.revrobotics.ColorSensorV3;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorMatch;


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

	Color sensorValues;
	ColorMatchResult detectedColor;
	double IR;
	String colorString;

	Signal sensorRedSig;
	Signal sensorGrnSig;
	Signal sensorBluSig;
	Signal matchResultSig;
	Signal matchConfSig;

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private CasseroleColorSensor() {
		
		//Configure color sensor
		i2cPort = I2C.Port.kOnboard;
		m_colorSensor = new ColorSensorV3(i2cPort);

		//Init color match algorithm
		m_colorMatcher = new ColorMatch();
		m_colorMatcher.addColorMatch(RobotConstants.kBlueTarget);
		m_colorMatcher.addColorMatch(RobotConstants.kGreenTarget);
	 	m_colorMatcher.addColorMatch(RobotConstants.kRedTarget);
		 m_colorMatcher.addColorMatch(RobotConstants.kYellowTarget);
		 
		sensorRedSig   = new Signal("Color Sensor Red Intensity", "intensity");
		sensorGrnSig   = new Signal("Color Sensor Green Intensity", "intensity");
		sensorBluSig   = new Signal("Color Sensor Blue Intensity", "intensity");
		matchResultSig = new Signal("Color Sensor Match Color", "color");
		matchConfSig   = new Signal("Color Sensor Match Confidence", "pct");
	}

	// This method should be called once per loop.
	// It will sample the values from the sensor, and calculate what color it thinks things are
	public void update(){
		sensorValues = m_colorSensor.getColor();
		detectedColor = m_colorMatcher.matchClosestColor(sensorValues);

		double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000;
		sensorRedSig.addSample(sampleTimeMs, sensorValues.red);  
		sensorGrnSig.addSample(sampleTimeMs, sensorValues.green);  
		sensorBluSig.addSample(sampleTimeMs, sensorValues.blue);  
		matchResultSig.addSample(sampleTimeMs, revColorToCasseroleColor(detectedColor.color).value);
		matchConfSig.addSample(sampleTimeMs, detectedColor.confidence); 
	}


	//Returns the best-guess color seen by the sensor.
	public ControlPanelColor getColor(){
		//Do the mapping from CTRE's color represtation to Casserole's
		return revColorToCasseroleColor(detectedColor.color);
	}

	public ControlPanelColor revColorToCasseroleColor(Color input){
		if (input == RobotConstants.kBlueTarget) {
			return ControlPanelColor.kBLUE;
		} else if (input == RobotConstants.kRedTarget) {
			return ControlPanelColor.kRED;
		} else if (input == RobotConstants.kGreenTarget) {
			return ControlPanelColor.kGREEN;
		} else if (input == RobotConstants.kYellowTarget) {
			return ControlPanelColor.kYELLOW;
		} else {
			return ControlPanelColor.kUNKNOWN;
		}
	}

	//Get the confidence percentage as to how certain the sensor is of the color seen.
	public double getConfidence(){
		return detectedColor.confidence; 
	}


}