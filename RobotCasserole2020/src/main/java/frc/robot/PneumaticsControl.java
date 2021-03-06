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

import edu.wpi.first.wpilibj.Compressor;
import frc.lib.DataServer.Signal;
import frc.lib.Util.ExecutionTimeTracker;
import edu.wpi.first.wpilibj.AnalogInput;
import frc.robot.HumanInterface.DriverController;

public class PneumaticsControl {

    Compressor compressor;
    AnalogInput pressureSensor;

    Signal pressSig;
    Signal pressSwVallSig;
    Signal compCurrent;

    double v_supplied = 5;
    double p_min = 0;
    double p_max = 150;

    double curPressurePSI;

    ExecutionTimeTracker timeTracker;

    /* Singelton Stuff */
    private static PneumaticsControl pneumatics = null;

    public static synchronized PneumaticsControl getInstance() {
        if (pneumatics == null)
            pneumatics = new PneumaticsControl();
        return pneumatics;
    }

    private PneumaticsControl() {
        compressor = new Compressor(RobotConstants.PNEUMATICS_CONTROL_MODULE_CAN_ID);
        pressureSensor = new AnalogInput(RobotConstants.ANALOG_PRESSURE_SENSOR_PORT);
        pressSig = new Signal("Pneumatics Main System Pressure", "psi");
        pressSwVallSig = new Signal("Pneumatics Cutoff Switch State", "bool");
        compCurrent = new Signal("Pneumatics Compressor Current", "A");

        timeTracker = new ExecutionTimeTracker("PneumaticsControl", 0.03);

        // Kick off monitor in brand new thread.
        // Thanks to Team 254 for an example of how to do this!
        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        timeTracker.run(pneumatics, PneumaticsControl.class.getMethod("update"));
                        Thread.sleep(150);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        // Set up thread properties and start it off
        monitorThread.setName("CasserolePneumaticsControl");
        monitorThread.setPriority(Thread.MIN_PRIORITY);
        monitorThread.start();

    }

    public Compressor getCompressor() {
        return compressor;
    }

    public void update() {
        double voltage = pressureSensor.getVoltage();

        curPressurePSI = (((p_max - p_min) * (1 - (0.1 * (v_supplied / voltage)))) / (0.8 * (v_supplied / voltage)))
                + p_min;
        curPressurePSI = (((p_max - (p_max * 0.1 * (v_supplied / voltage)))) / (0.8 * (v_supplied / voltage)));
        /* actual equation but pmin is zero so we can simplify */

        if (v_supplied >= 0.001) {
            curPressurePSI = (250 * (voltage / 4.62) - 25);
        } else {
            curPressurePSI = 0;// meh, should never happen physically
        }

        if (DriverController.getInstance().getCompressorDisableReq()) {
            this.stop();
        } else if (DriverController.getInstance().getCompressorEnableReq()) {
            this.start();
        }

        double sample_time_ms = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        pressSig.addSample(sample_time_ms, curPressurePSI);
        pressSwVallSig.addSample(sample_time_ms, compressor.getPressureSwitchValue());
        compCurrent.addSample(sample_time_ms, compressor.getCompressorCurrent());
    }

    // start method for the compressor
    public void start() {
        compressor.start();
    }

    // stop method for the compressor
    public void stop() {
        compressor.stop();
    }

    public double getPressure() {
        return curPressurePSI;
    }
}