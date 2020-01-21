package frc.robot.ControlPanel;

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
    int degreesToRotateColor;
    boolean prevXBtn=false;
    boolean prevYBtn=false;
    int degreesToRotateThreeToFive;

    String gameData = DriverStation.getInstance().getGameSpecificMessage();

    ControlPanelColor gameDataColor = ControlPanelColor.kUNKNOWN;
    
    Signal degreesToRotateStaticSig;
    Signal degreesToColorSig;
    Signal colorNeededSig;



    private ControlPanelStateMachine(){
        colorSensor = CasseroleColorSensor.getInstance();
        degreesToRotateStaticSig = new Signal("Panel 3.25 spin","degrees");
        degreesToColorSig = new Signal("Rotate to Color","degrees");
        colorNeededSig = new Signal("Color Needed","Color");
    }

    public int degreesToTurn(ControlPanelColor colorOnWheel, String gameData){
        //convert gameData to rotational information

        int desiredRotation = 0;
        if(gameDataColor == ControlPanelColor.kRED){
            desiredRotation=0;
        }else if(gameDataColor == ControlPanelColor.kGREEN){
            desiredRotation=45;
        }else if(gameDataColor == ControlPanelColor.kBLUE){
            desiredRotation=90;
        }else if(gameDataColor == ControlPanelColor.kYELLOW){
            desiredRotation=135;
        }else{ 
            //10 is used to correct and/or move so the color seonsor can see the exact color
            desiredRotation=10;
        }
        
        int rotateCmd=desiredRotation-(colorOnWheel.value*45);

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
        

        operatorXButtonPressed = OperatorController.getInstance().getControlPanelThreeRotationsDesired();
        operatorYButtonPressed = OperatorController.getInstance().getControlPanelSeekToColorDesired();

        if(prevXBtn != operatorXButtonPressed && prevXBtn==false){
            degreesToRotateThreeToFive = degreesToTurn(colorOnWheel, gameData);
        }else if(prevXBtn != operatorXButtonPressed){
            degreesToRotateThreeToFive = 0;
        }

        if(prevYBtn != operatorYButtonPressed && prevYBtn==false){
            degreesToRotateColor = degreesToTurn(colorOnWheel, gameData);
        }else if(prevYBtn != operatorYButtonPressed){
            degreesToRotateColor= 0;
        }

        
        colorSensor.update();
        prevXBtn=operatorXButtonPressed;
        prevYBtn=operatorYButtonPressed;
    }

    public int getdegreesToRotateThreeToFive(){
       return this.degreesToRotateThreeToFive;
    }

    public int getdegreesToRotateColor(){
       return this.degreesToRotateColor;
    }
}