package frc.robot;

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

import edu.wpi.first.wpilibj.Timer;
import frc.lib.DataServer.Annotations.Signal;

public class LoopTiming{

    //All times in seconds
    double loopStartTime;
    double loopEndTime;

    double prevLoopStartTime;
    double prevLoopEndTime;

    @Signal
    double loopPeriodSec;

    @Signal
    double loopDurationSec;


    /* Singleton stuff */
    private static LoopTiming loopTiming = null;
    public static synchronized LoopTiming getInstance() {
        if(loopTiming == null)
            loopTiming = new LoopTiming();
        return loopTiming;
    }

    private LoopTiming(){

    }

    public void markLoopStart(){
        prevLoopStartTime = loopStartTime;
        loopStartTime = Timer.getFPGATimestamp();
        loopPeriodSec = loopStartTime - prevLoopStartTime;
    }

    public void markLoopEnd(){
        prevLoopEndTime = loopEndTime;
        loopEndTime = Timer.getFPGATimestamp();
        loopDurationSec = loopEndTime - loopStartTime;
    }

    public double getLoopStartTimeSec(){
        return loopStartTime;
    }

    public double getPeriodSec(){
        return loopPeriodSec;
    }
}