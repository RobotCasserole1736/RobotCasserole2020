package frc.robot.HumanInterface;
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
 *  Please feel free to snag our software for your own use in whatever project
 *   you have going on right now! We'd love to be able to help out! Shoot us 
 *   any questions you may have, all our contact info should be on our website
 *   (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *   Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *   if you would consider donating to our club to help further STEM education.
 */

 /**
  * This class converts a boolean representing a pushed or unpushed button to an
  *  intuitive pulse-train of booleans, which can be used to trigger repeated actions.
  *
  *  It's currently set up for two modes of operation: If the button is held down, 
  *  there will be three slow triggers, followed by unlimited fast triggers. One trigger
  *  always happens right as the button is pressed.
  *
  * The goal is to convert up/down buttons into intuitive value adjustments. Tapping the button
  *  will produce a predictable number of adjustements. Holding the button will cause the adjustments
  *  to trigger quickly. This allows fine and coarse adjustments to be done from a single button interface.
  */
public class ButtonPulser {

    int activeLoopCounter = 0;
    int slowPulseCounter = 0;

    boolean isSlowMode = true;

    final int SLOW_PULSE_MAX_COUNT = 3;
    final int SLOW_PULSE_PERIOD_LOOPS = 25;

    final int FAST_PULSE_PERIOD_LOOPS = 3;


    public ButtonPulser(){
        return;
    }

    public boolean pulse(boolean input){
        boolean retVal = false;

        if(input == false){
            //Imedeate disable and reset on button release
            retVal = false;
            isSlowMode = true;
            activeLoopCounter = 0;
        } else {
            if(isSlowMode){
                //On press, first do a series of slow pulses
                if(activeLoopCounter % SLOW_PULSE_PERIOD_LOOPS == 0){
                    retVal = true;
                    slowPulseCounter++;
                } else {
                    retVal = false;
                }
            } else { 
                //Then, if the button is continued to be held, do a series of fast pulses
                retVal = (activeLoopCounter%FAST_PULSE_PERIOD_LOOPS == 0);
            }
            activeLoopCounter++;
        }

        return retVal;
    }

}