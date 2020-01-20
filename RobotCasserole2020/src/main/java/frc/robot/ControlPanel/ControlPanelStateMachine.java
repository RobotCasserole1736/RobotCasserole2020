package frc.robot.ControlPanel;

import frc.robot.LoopTiming;
import frc.robot.ControlPanel.CasseroleColorSensor;
import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.HumanInterface.OperatorController;
import frc.lib.DataServer.Signal;

public class ControlPanelStateMachine{

    /* Singelton Stuff */
    private static ControlPanelStateMachine instance = null;
    
    
    CasseroleColorSensor colorSensor;
    

    public static synchronized ControlPanelStateMachine getInstance() {
        if(instance == null)
        instance = new ControlPanelStateMachine();
        return instance;
    }

    ControlPanelColor colorOnWheel = CasseroleColorSensor.getInstance().getControlPanelColor();
    ControlPanelColor gameDataColor = ControlPanelColor.kUNKNOWN;

    boolean prevXBtn=false;
    boolean prevYBtn=false;

    int degreesToRotateColor;
    int degreesToRotateThreeToFive;

    double sampleTimeMS;

    String gameData = DriverStation.getInstance().getGameSpecificMessage();
    
    Signal degreesToRotateStaticSig;
    Signal degreesToColorSig;
    Signal colorNeededSig;

    private ControlPanelStateMachine(){
        colorSensor = CasseroleColorSensor.getInstance();
        degreesToRotateStaticSig = new Signal("Panel 3.25 spin","degrees");
        degreesToColorSig = new Signal("Rotate to Color","degrees");
        colorNeededSig = new Signal("Color Needed","Color");
    }

    public void parseGameData(String gameData){
        String desiredColor=String.valueOf(gameData.charAt(0));
        if(desiredColor.length() > 0){
            if(desiredColor.equals("R")){
                gameDataColor=ControlPanelColor.kRED;
            }else if(desiredColor.equals("G")){
                gameDataColor=ControlPanelColor.kGREEN;
            }else if(desiredColor.equals("B")){
                gameDataColor=ControlPanelColor.kBLUE;
            }else if(desiredColor.equals("Y")){
                gameDataColor=ControlPanelColor.kYELLOW;
            }else{
                gameDataColor=ControlPanelColor.kUNKNOWN;
            }
        } else {
            gameDataColor=ControlPanelColor.kUNKNOWN;
        }
    }

    public int degreesToTurn(ControlPanelColor colorOnWheel){
        int rotateCmd=10;
        if(gameDataColor!=ControlPanelColor.kUNKNOWN){
            rotateCmd=(gameDataColor.value*45)-(colorOnWheel.value*45);
        }

        //checks if there is a 135 degree rotation. If so sets it to it 45 degrees in the opposite direction
        if(Math.abs(rotateCmd)>90){
            rotateCmd=(rotateCmd/-3);
        }

        return rotateCmd;
    }

    public int setRotateStage2Desired(boolean rotateStage2Cmd_in){
        //rotates the wheel 3.25 times (1170 degrees) giving us leeway to rotate the last 360 degrees
        int rotateCmd = 1170;
        return rotateCmd;
    }

    public void update(){

        boolean operatorXButtonPressed;
        boolean operatorYButtonPressed;

        sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;

        parseGameData(gameData);

        operatorXButtonPressed = OperatorController.getInstance().getControlPanelThreeRotationsDesired();
        operatorYButtonPressed = OperatorController.getInstance().getControlPanelSeekToColorDesired();

        if(prevXBtn != operatorXButtonPressed && prevXBtn==false){
            degreesToRotateThreeToFive = degreesToTurn(colorOnWheel);
        }else if(prevXBtn != operatorXButtonPressed){
            degreesToRotateThreeToFive = 0;
        }

        if(prevYBtn != operatorYButtonPressed && prevYBtn==false){
            degreesToRotateColor = degreesToTurn(colorOnWheel);
        }else if(prevYBtn != operatorYButtonPressed){
            degreesToRotateColor= 0;
        }

        
        colorSensor.update();
        prevXBtn=operatorXButtonPressed;
        prevYBtn=operatorYButtonPressed;

        degreesToColorSig.addSample(sampleTimeMS, degreesToRotateColor);
        degreesToRotateStaticSig.addSample(sampleTimeMS, degreesToRotateThreeToFive);
        colorNeededSig.addSample(sampleTimeMS, gameDataColor.value);
    }

    public int getdegreesToRotateThreeToFive(){
       return this.degreesToRotateThreeToFive;
    }

    public int getdegreesToRotateColor(){
       return this.degreesToRotateColor;
    }
}