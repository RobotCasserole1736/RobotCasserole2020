package frc.robot;

import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.DriverStation;

public class LEDController {

    private static LEDController ledCtrl = null;

    PWM ctrl;

    private DriverStation.Alliance curAlliance;

    public static synchronized LEDController getInstance() {
        if (ledCtrl == null)
            ledCtrl = new LEDController();
        return ledCtrl;
    }

    public enum LEDPatterns {
        Pattern0(0), // Red Color Sparkle
        Pattern1(1), // Blue Color Sparkle
        Pattern4(4), // Blue Fade
        Pattern5(5), // Red Fade
        Pattern6(6), // Rainbow Fade Chase
        PatternDisabled(-1); // CasseroleColorStripeChase

        public final int value;

        private LEDPatterns(int value) {
            this.value = value;
        }

        public int toInt() {
            return this.value;
        }
    }

    // This is the private constructor that will be called once by getInstance() and
    // it
    // should instantiate anything that will be required by the class
    private LEDController() {

        ctrl = new PWM(RobotConstants.LED_CONTROLLER_PORT);

        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        ledUpdater();
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Set up thread properties and start it off
        monitorThread.setName("CasseroleLEDThread");
        monitorThread.setPriority(Thread.MIN_PRIORITY);
        monitorThread.start();
    }

    /*
     * This is a Utility Function to tell the updater what alliance color we are
     */

    public void teamColor(){
        curAlliance = DriverStation.getInstance().getAlliance();
    }

    public void ledUpdater() {
        if (DriverStation.getInstance().getMatchTime() <= 30 && Climber.getInstance().climbEnabled == true) {
            // ledController.setPattern(LEDPatterns.Pattern6);
            ctrl.setSpeed(1.0);
        } else if (curAlliance == DriverStation.Alliance.Blue) {
            if (DriverStation.getInstance().isAutonomous() == true) {
                ctrl.setSpeed(-0.5);
            } else {
                ctrl.setSpeed(0.25);
            }
        } else if (curAlliance == DriverStation.Alliance.Red) {
            if (DriverStation.getInstance().isAutonomous() == true) {
                ctrl.setSpeed(-1.0);
            } else {
                ctrl.setSpeed(0.5);
            }
        }

    }
}