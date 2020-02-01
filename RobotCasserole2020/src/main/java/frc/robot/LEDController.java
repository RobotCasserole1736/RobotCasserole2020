package frc.robot;
import edu.wpi.first.wpilibj.PWM;

public class LEDController {

    private static LEDController ledCtrl = null;

    PWM ctrl;

    private LEDPatterns patternCmd;



    public static synchronized LEDController getInstance() {
        if(ledCtrl == null)
            ledCtrl = new LEDController();
        return ledCtrl;
    }

    public enum LEDPatterns {
        Pattern0(0), // TODO - put what pattern this actually is
        Pattern1(1), // TODO - put what pattern this actually is
        Pattern2(2), // TODO - put what pattern this actually is
        Pattern3(3), // TODO - put what pattern this actually is
        Pattern4(4), // TODO - put what pattern this actually is
        Pattern5(5), // TODO - put what pattern this actually is
        Pattern6(6), // TODO - put what pattern this actually is
        Pattern7(7), // TODO - put what pattern this actually is
        PatternDisabled(-1); // CasseroleColorStripeChase
     

        public final int value;

        private LEDPatterns(int value) {
            this.value = value;
        }
                
        public int toInt(){
            return this.value;
        }
    }

    // This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
    private LEDController() {
        
        patternCmd = LEDPatterns.Pattern0;
        ctrl = new PWM(RobotConstants.LED_CONTROLLER_PORT);
    }

    public void update(){
        switch(patternCmd){
            case Pattern0:
                ctrl.setSpeed(-1.0);
                //this should be 0900-0999ms
                //this happens in Auto
            break;
            case Pattern1:
                ctrl.setSpeed(-0.5);
                //this sould be 1200-1299ms
                //this happens in Teleop
            break;
            case Pattern2:
                ctrl.setSpeed(-0.25);
                //this should be 1300-1449ms
                //currently not assigned
            break;
            case Pattern3:
                ctrl.setSpeed(0.0);
                //this should be 1450-1499ms
                //currently not assigned
            break;
            case Pattern4:
                ctrl.setSpeed(0.25);
                //this should be 1600-1699ms
                //currently not assigned
            break;
            case Pattern5:
                ctrl.setSpeed(0.5);
                //this should be 1700-1799ms
                //currently not assigned
            break;
            case Pattern6:
                ctrl.setSpeed(0.75);
                //this should be 1800-1900ms
                //currently not assigned
            break;
            case Pattern7:
                ctrl.setSpeed(1.0);
                //this should be 1901-2000ms
                //currently not assigned
            break;
            default:
                //Do Nothing - disabled happens automatically by roboRIO
            break;
        }
    }

    public void setPattern(LEDPatterns pattern_in){
        patternCmd = pattern_in;
    }

    public void setDisabledPattern(){
        patternCmd = LEDPatterns.Pattern0;
    }
}