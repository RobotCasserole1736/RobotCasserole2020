package frc.robot;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.DriverStation;

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
                
        public int toInt(){
            return this.value;
        }
    }

    // This is the private constructor that will be called once by getInstance() and it
    //should instantiate anything that will be required by the class
    private LEDController() {
        
        patternCmd = LEDPatterns.Pattern0;
        ctrl = new PWM(RobotConstants.LED_CONTROLLER_PORT);
    }

    public void update(){
        switch(patternCmd){
            case Pattern0:
                ctrl.setSpeed(-1.0);
                //this should be 0900-0999ms
                //TODO - State when this is played
            break;
            case Pattern1:
                ctrl.setSpeed(-0.5);
                //this sould be 1200-1299ms
                //TODO - State when this is played
            break;
            case Pattern4:
                ctrl.setSpeed(0.25);
                //this should be 1600-1699ms
                //TODO - State when this is played
            break;
            case Pattern5:
                ctrl.setSpeed(0.5);
                //this should be 1700-1799ms
                //TODO - State when this is played
            break;
            case Pattern6:
                ctrl.setSpeed(1.0);
                //this should be 1901-2000ms
                //TODO - State when this is played
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
/*This is a Utility Function to tell the updater
 *what alliance color we are*/

public void teamColor(){
    
    if (DriverStation.getInstance().getAlliance() == DriverStation.Alliance.Red)
}

public void ledUpdater(){
    if (DriverStation.getInstance().getMatchTime() <= 30 && Climber.getInstance().climbEnabled == true){
      //ledController.setPattern(LEDPatterns.Pattern6);
      ctrl.setSpeed(1.0);
    }
    else if(DriverStation.getInstance().getAlliance() == DriverStation.Alliance.Blue){
      if(DriverStation.getInstance().isAutonomous() == true){
        ctrl.setSpeed(-0.5);
      }
      else{
        ctrl.setSpeed(0.25);
      }
    }
    else if(DriverStation.getInstance().getAlliance() == DriverStation.Alliance.Red){
      if(DriverStation.getInstance().isAutonomous() == true){
        ctrl.setSpeed(-1.0);
      }
      else{
        ctrl.setSpeed(0.5);
      }
    }
    Thread monitorThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()){
                    periodicUpdate();
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    
    rioCPULoad = new Signal("roboRIO CPU Load", "pct");
    rioMemLoad = new Signal("roboRIO Sys Memory Load", "pct"); 
    rioJVMMemLoad = new Signal("roboRIO JVM Memory Load", "pct"); 
    
    //Set up thread properties and start it off
    monitorThread.setName("CasseroleRIOLoadMonitor");
    monitorThread.setPriority(Thread.MIN_PRIORITY);
    monitorThread.start();
}
  }