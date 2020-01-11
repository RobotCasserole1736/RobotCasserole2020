package frc.robot;

import edu.wpi.first.wpilibj.GenericHID;

public class CasseroleCustomController extends GenericHID {

    private int loopCounter = 0;

    public enum LedState 
    { 
        OFF, ON, BLINK_SLOW, BLINK_FAST 
    } 
      

    /* Electrical Wiring constants */
    private final int BTN_UPPER_RED = 9;  //What The fork driverstation. Figured this out imperically. Something between DS and this code is screwy.
    private final int BTN_UPPER_YEL = 10;
    private final int BTN_UPPER_GRN = 11;
    private final int BTN_UPPER_BLU = 12;
    private final int BTN_UPPER_BLK = 13;
    private final int BTN_LOWER_RED = 14;
    private final int BTN_LOWER_YEL = 15;
    private final int BTN_LOWER_GRN = 16;
    private final int BTN_LOWER_BLU = 1;
    private final int BTN_LOWER_BLK = 2;
    private final int BTN_JYSTK_TOP = 3;
    private final int NUM_BTN =  17;

    private final int LED_LOWER_RED = 0;
    private final int LED_LOWER_YEL = 1;
    private final int LED_LOWER_GRN = 2;
    private final int LED_LOWER_BLU = 3;
    private final int LED_LOWER_WHT = 4;
    private final int LED_UPPER_RED = 5;
    private final int LED_UPPER_YEL = 6;
    private final int LED_UPPER_GRN = 7;
    private final int LED_UPPER_BLU = 8;
    private final int LED_UPPER_WHT = 9;
    private final int NUM_LEDS = 10;

    private final int AXIS_JOY_X = 0;
    private final int AXIS_JOY_Y = 1;
    private final int AXIS_JOY_TWIST = 2;
    private final int AXIS_UPPER_KNOB = 7;
    private final int AXIS_LOWER_KNOB = 5;
    private final int NUM_AXIS = 8;

    /* Output LED states */
    private boolean LEDStateCommands[] = new boolean[NUM_LEDS];
    private LedState LEDStates[] = new LedState[NUM_LEDS];

    private double deadzone(double input){
        if(Math.abs(input) < 0.05){
            return 0.0;
        } else {
            return input;
        }
    }

    public CasseroleCustomController(int port){
        super(port);
    }


    public void ledUpdate(){
        loopCounter++;

        //this.setOutputs(getLEDCommandInt());
        int setVal = 0xFFFFFFF0 | loopCounter;
        this.setOutputs(setVal); //temp - test only
        System.out.printf("%x\n", setVal);
    }

    public int getLEDCommandInt(){
        int retVal = 0;
        retVal |= LEDStateCommands[LED_UPPER_RED]?1:0 << (LED_UPPER_RED - 1);
        retVal |= LEDStateCommands[LED_UPPER_YEL]?1:0 << (LED_UPPER_YEL - 1);
        retVal |= LEDStateCommands[LED_UPPER_GRN]?1:0 << (LED_UPPER_GRN - 1);
        retVal |= LEDStateCommands[LED_UPPER_BLU]?1:0 << (LED_UPPER_BLU - 1);
        retVal |= LEDStateCommands[LED_UPPER_WHT]?1:0 << (LED_UPPER_WHT - 1);
        retVal |= LEDStateCommands[LED_LOWER_RED]?1:0 << (LED_LOWER_RED - 1);
        retVal |= LEDStateCommands[LED_LOWER_YEL]?1:0 << (LED_LOWER_YEL - 1);
        retVal |= LEDStateCommands[LED_LOWER_GRN]?1:0 << (LED_LOWER_GRN - 1);
        retVal |= LEDStateCommands[LED_LOWER_BLU]?1:0 << (LED_LOWER_BLU - 1);
        retVal |= LEDStateCommands[LED_LOWER_WHT]?1:0 << (LED_LOWER_WHT - 1);
        return retVal;
    }

    // PUBLIC API
    public boolean getRedUpperButton(){ return this.getRawButton(BTN_UPPER_RED);}
    public boolean getYellowUpperButton(){ return this.getRawButton(BTN_UPPER_YEL);}
    public boolean getGreenUpperButton(){ return this.getRawButton(BTN_UPPER_GRN);}
    public boolean getBlueUpperButton(){ return this.getRawButton(BTN_UPPER_BLU);}
    public boolean getBlackUpperButton(){ return this.getRawButton(BTN_UPPER_BLK);}
    public boolean getRedLowerButton(){ return this.getRawButton(BTN_LOWER_RED);}
    public boolean getYellowLowerButton(){ return this.getRawButton(BTN_LOWER_YEL);}
    public boolean getGreenLowerButton(){ return this.getRawButton(BTN_LOWER_GRN);}
    public boolean getBlueLowerButton(){ return this.getRawButton(BTN_LOWER_BLU);}
    public boolean getBlackLowerButton(){ return this.getRawButton(BTN_LOWER_BLK);}
    public boolean getJoystickButton(){ return this.getRawButton(BTN_JYSTK_TOP);}

    public double getJoystickX(){return deadzone(-1*this.getRawAxis(AXIS_JOY_X));} //1.0 = right, -1.0 = left, 0.0 = center
    public double getJoystickY(){return deadzone(this.getRawAxis(AXIS_JOY_Y));} //1.0 = down, -1.0 = up, 0.0 = center
    public double getJoystickTwist(){return deadzone(this.getRawAxis(AXIS_JOY_TWIST));} //1.0 = CW Twist, -1.0 = CCW twist, 0.0 = center
    public double getUpperKnob(){return 0.5+0.5*this.getRawAxis(AXIS_UPPER_KNOB);} // Range - 0 for full CCW, 1 for full CW
    public double getLowerKnob(){return 0.5+0.5*this.getRawAxis(AXIS_LOWER_KNOB);} // Range - 0 for full CCW, 1 for full CW

    public void setRedUpperLED(LedState cmd){    LEDStates[LED_UPPER_RED] = cmd; }
    public void setYellowUpperLED(LedState cmd){ LEDStates[LED_UPPER_YEL] = cmd; }
    public void setGreenUpperLED(LedState cmd){  LEDStates[LED_UPPER_GRN] = cmd; }
    public void setBlueUpperLED(LedState cmd){   LEDStates[LED_UPPER_BLU] = cmd; }
    public void setWhiteUpperLED(LedState cmd){  LEDStates[LED_UPPER_WHT] = cmd; }
    public void setRedLowerLED(LedState cmd){    LEDStates[LED_LOWER_RED] = cmd; }
    public void setYellowLowerLED(LedState cmd){ LEDStates[LED_LOWER_YEL] = cmd; }
    public void setGreenLowerLED(LedState cmd){  LEDStates[LED_LOWER_GRN] = cmd; }
    public void setBlueLowerLED(LedState cmd){   LEDStates[LED_LOWER_BLU] = cmd; }
    public void setWhiteLowerLED(LedState cmd){  LEDStates[LED_LOWER_WHT] = cmd; }

    @Override
    public double getX(Hand hand) {
        return getJoystickX();
    }

    @Override
    public double getY(Hand hand) {
        return getJoystickY();
    }

    public void debugPrint(){
        for(int i = 0; i < NUM_BTN; i++){
            System.out.printf("RawBtn %d: %b \n", i, this.getRawButton(i));
        }
        for(int i = 0; i < NUM_AXIS; i++){
            System.out.printf("RawAxis %d: %f \n", i, this.getRawAxis(i));
        }
        System.out.println("============================");
    }
}