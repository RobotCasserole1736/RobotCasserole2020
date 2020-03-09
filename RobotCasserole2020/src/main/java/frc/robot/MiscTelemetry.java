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

import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import frc.lib.DataServer.CasseroleDataServer;
import frc.lib.DataServer.Signal;
import frc.lib.Util.ExecutionTimeTracker;

public class MiscTelemetry {

    /* Singelton Stuff */
    private static MiscTelemetry inst = null;

    boolean climberUpperLSPressed;
    boolean climberLowerLSPressed;
    boolean pneumaticPressureLow;
    Climber climber;

    //Top level telemetry signals
    Signal rioDSSampLoadSig;
    Signal rioDSLogQueueLenSig;
    Signal rioBattCurrDrawSig;
    Signal rioBattVoltSig;
    Signal rioSupplyVoltSig;
    Signal rioIsBrownoutSig;
    Signal rio6VBusVoltageSig;
    Signal rio5VBusVoltageSig;
    Signal rio3V3BusVoltageSig;
    Signal rioCANBusUsagePctSig;
    Signal pdpUpperBoardAuxCurrentSig;
    Signal pdpCoolingFansCurrentSig;

    ExecutionTimeTracker timeTracker;

    public static synchronized MiscTelemetry getInstance() {
        if (inst == null)
            inst = new MiscTelemetry();
        return inst;
    }

    private MiscTelemetry(){
        climber = Climber.getInstance();

        /* Init local telemetry signals */
        rioDSSampLoadSig = new Signal("Dataserver Stored Samples", "count"); 
        rioBattCurrDrawSig = new Signal("Robot Battery Current Draw", "A");
        rioBattVoltSig = new Signal("Robot Battery Voltage", "V");
        rioSupplyVoltSig = new Signal("RIO Input Voltage", "V");
        rio3V3BusVoltageSig = new Signal("RIO 3V Supply Voltage", "V");
        rio5VBusVoltageSig = new Signal("RIO 5V Supply Voltage", "V");
        rio6VBusVoltageSig = new Signal("RIO 6V Supply Voltage", "V");
        rioDSLogQueueLenSig = new Signal("Dataserver File Logger Queue Length", "count");
        rioIsBrownoutSig = new Signal("Robot Brownout", "bool");
        rioCANBusUsagePctSig = new Signal("Robot CAN Bus Utilization", "pct");
        pdpUpperBoardAuxCurrentSig = new Signal("PDP Upper Board Current", "A");
        pdpCoolingFansCurrentSig = new Signal("PDP Cooling Fans Current", "A");

        timeTracker = new ExecutionTimeTracker("Misc Telemetry", 0.03);

        
        // Kick off monitor in brand new thread.
        // Thanks to Team 254 for an example of how to do this!
        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        timeTracker.run(inst, MiscTelemetry.class.getMethod("telemetryUpdate"));
                        Thread.sleep(150);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        // Set up thread properties and start it off
        monitorThread.setName("CasseroleMiscTelemetry");
        monitorThread.setPriority(Thread.MIN_PRIORITY);
        monitorThread.start();
    }

    public void telemetryUpdate(){
        double sampleTimeMs = Timer.getFPGATimestamp()*1000.0;

        rioDSSampLoadSig.addSample(sampleTimeMs, CasseroleDataServer.getInstance().getTotalStoredSamples());
        rioBattCurrDrawSig.addSample(sampleTimeMs,  CasserolePDP.getInstance().getTotalCurrent());
        rioBattVoltSig.addSample(sampleTimeMs,  CasserolePDP.getInstance().getVoltage());  
        rioSupplyVoltSig.addSample(sampleTimeMs,  RobotController.getInputVoltage());  
        rioDSLogQueueLenSig.addSample(sampleTimeMs, CasseroleDataServer.getInstance().logger.getSampleQueueLength());
        rioIsBrownoutSig.addSample(sampleTimeMs, RobotController.isBrownedOut());
        rioCANBusUsagePctSig.addSample(sampleTimeMs, RobotController.getCANStatus().percentBusUtilization);
        pdpUpperBoardAuxCurrentSig.addSample(sampleTimeMs, CasserolePDP.getInstance().getCurrent(RobotConstants.UPPER_BOARD_AUX_PDP_CHANNEL));
        pdpCoolingFansCurrentSig.addSample(sampleTimeMs, CasserolePDP.getInstance().getCurrent(RobotConstants.COOLING_FANS_PDP_CHANNEL));
      }
        

}