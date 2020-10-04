package frc.robot.ControlPanel;
/*
 *******************************************************************************************
 * Copyright (C) 2020 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *   find it educational and informative! 
 *  Please feel free to snag package frc.robot.ControlPanel;
/*
 *******************************************************************************************
 * Copyright (C) 2our software for your own use in whatever project
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

import com.revrobotics.ColorSensorV3;
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

	ControlPanelColor controlPanelColor;

	double[] redMin = {0.34, 0.36, 0.10};
	double[] redMax = {0.54, 0.45, 0.22};
	double[] greenMin = {0.17, 0.49, 0.22};
	double[] greenMax = {0.28, 0.57, 0.27};
	double[] blueMin = {0.12, 0.41, 0.26};
	double[] blueMax = {0.24, 0.47, 0.45};
	double[] yellowMin = {0.30, 0.48, 0.10};
	double[] yellowMax = {0.40, 0.56, 0.21};

	Color sensorValues;
	double IR;
	String colorString;

	Signal sensorRedSig;
	Signal sensorGrnSig;
	Signal sensorBluSig;
	Signal matchResultSig;

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private CasseroleColorSensor() {
		
		controlPanelColor = ControlPanelColor.kUNKNOWN;

		//Configure color sensor
		i2cPort = I2C.Port.kOnboard;
		m_colorSensor = new ColorSensorV3(i2cPort);

		//Init color match algorithm
		m_colorMatcher = new ColorMatch();
		 
		sensorRedSig   = new Signal("Color Sensor Red Intensity", "sat");
		sensorGrnSig   = new Signal("Color Sensor Green Intensity", "sat");
		sensorBluSig   = new Signal("Color Sensor Blue Intensity", "sat");
		matchResultSig = new Signal("Color Sensor Match Color", "color");
	}
	

	// This method should be called once per loop.
	// It will sample the values from the sensor, and calculate what color it thinks things are
	public void update(){
		sensorValues = m_colorSensor.getColor();
		
		double[] sensorValueList={sensorValues.red,sensorValues.green,sensorValues.blue};

		controlPanelColor = colorOfWheel(sensorValueList);

		double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000;
		sensorRedSig.addSample(sampleTimeMs, sensorValues.red);  
		sensorGrnSig.addSample(sampleTimeMs, sensorValues.green);  
		sensorBluSig.addSample(sampleTimeMs, sensorValues.blue); 
		matchResultSig.addSample(sampleTimeMs, controlPanelColor.value);
	}
	
	public boolean inRange(double[] inList,double[]min,double[]max){
		boolean retVal=true;
		for(int i = 0; i < 3; i++){
			if(inList[i]>max[i]||inList[i]<min[i]){
				retVal=false;
			}
		}
		return retVal;
	}

	public ControlPanelColor colorOfWheel(double[] sensorValueList){
		if(inRange(sensorValueList, redMin, redMax)){
			return ControlPanelColor.kRED;
		} else if(inRange(sensorValueList, greenMin, greenMax)){
			return ControlPanelColor.kGREEN;
		} else if(inRange(sensorValueList, blueMin, blueMax)){
			return ControlPanelColor.kBLUE;
		} else if(inRange(sensorValueList, yellowMin, yellowMax)){
			return ControlPanelColor.kYELLOW;
		} else {
			return ControlPanelColor.kUNKNOWN;
		}
	}

	public ControlPanelColor getControlPanelColor(){
        return this.controlPanelColor;
	}
 }