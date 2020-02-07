package frc.robot.ControlPanel;

import frc.robot.LoopTiming;
import frc.robot.ControlPanel.CasseroleColorSensor;
import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.HumanInterface.OperatorController;
import frc.lib.DataServer.Signal;
import frc.robot.ControlPanel.ControlPanelManipulator;

public class ControlPanelStateMachine{

    /* Singelton Stuff */
    private static ControlPanelStateMachine instance = null;

    public static synchronized ControlPanelStateMachine getInstance() {
        if(instance == null)
        instance = new ControlPanelStateMachine();
        return instance;
    }
        
    CasseroleColorSensor colorSensor;

    ControlPanelColor colorOnWheel = ControlPanelColor.kUNKNOWN;
    ControlPanelColor gameDataColor = ControlPanelColor.kUNKNOWN;

    boolean prevRotate3To5State=false;
    boolean prevGoToColorState=false;

    int degreesToRotateColor;
    int degreesToRotateThreeToFive;

    double sampleTimeMS;

    Signal degreesToRotateStaticSig;
    Signal degreesToColorSig;
    Signal colorNeededSig;


    private ControlPanelStateMachine(){
        colorSensor = CasseroleColorSensor.getInstance();
        degreesToRotateStaticSig = new Signal("Control Panel State Machine 3.25 spin","degrees");
        degreesToColorSig = new Signal("Control Panel State Machine Rotate to Color","degrees");
        colorNeededSig = new Signal("Control Panel State Machine Game Data Color Command","color");
    }

    public void update(){

        boolean rotate3to5Activated;
        boolean rotateToSpecificColorActivated;

        sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;

        colorSensor.update();
        parseGameData(DriverStation.getInstance().getGameSpecificMessage());

        colorOnWheel = CasseroleColorSensor.getInstance().getControlPanelColor();

        rotate3to5Activated = OperatorController.getInstance().getControlPanelThreeRotationsDesired();
        rotateToSpecificColorActivated = OperatorController.getInstance().getControlPanelSeekToColorDesired();

        if(rotate3to5Activated && prevRotate3To5State==false){
            degreesToRotateThreeToFive = setRotateStage2Desired();
        }else{
            degreesToRotateThreeToFive = 0;
        }

        if(rotateToSpecificColorActivated && prevGoToColorState==false){
            degreesToRotateColor = degreesToTurn(colorOnWheel);
        }else{
            degreesToRotateColor= 0;
        }

        prevRotate3To5State=rotate3to5Activated;
        prevGoToColorState=rotateToSpecificColorActivated;

        if(degreesToRotateThreeToFive != 0 && degreesToRotateColor == 0){
            ControlPanelManipulator.getInstance().sendRotationCommand(degreesToRotateThreeToFive);
        }else if(degreesToRotateColor != 0 && degreesToRotateThreeToFive == 0){
            ControlPanelManipulator.getInstance().sendRotationCommand(degreesToRotateColor);
        }else{
            ControlPanelManipulator.getInstance().sendRotationCommand(0);
        }

        degreesToRotateStaticSig.addSample(sampleTimeMS, degreesToRotateThreeToFive);
        degreesToColorSig.addSample(sampleTimeMS, degreesToRotateColor);
        colorNeededSig.addSample(sampleTimeMS, gameDataColor.value);
    }

    public void parseGameData(String gameData){
        if(gameData.length() > 0){
            String desiredColor=String.valueOf(gameData.charAt(0));
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
        }else{
            gameDataColor=ControlPanelColor.kUNKNOWN;
        }

        //Changes the gameData color to something in our orientation since the panel color sensor
        //is off to the side and not where we are (90 degree transformation)
        if(gameDataColor != ControlPanelColor.kUNKNOWN){
            int colorRotation;
            ControlPanelColor colorActual;
            //The -2 signifies going to 2 index values before what we are currently at
            //this is effectively a 90 degree rotation
            colorRotation = gameDataColor.value-2;
            //this function returns the color we need to be at from the index we gave it
            colorActual = ControlPanelColor.getColorFromInt(colorRotation);
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

    public int setRotateStage2Desired(){
        //rotates the wheel 3.25 times (1170 degrees) giving us leeway to rotate the last 360 degrees
        int rotateCmd = 1170;
        return rotateCmd;
    }


    public ControlPanelColor getGameDataColor(){
       return this.gameDataColor;
    }

}