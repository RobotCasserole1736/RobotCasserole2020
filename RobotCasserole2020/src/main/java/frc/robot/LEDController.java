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
        PatternDisabled(-1);
     

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
            break;
            case Pattern1:
                ctrl.setSpeed(-0.5);
            break;
            case Pattern2:
                ctrl.setSpeed(0.0);
            break;
            case Pattern3:
                ctrl.setSpeed(0.5);
            break;
            case Pattern4:
                ctrl.setSpeed(1.0);
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