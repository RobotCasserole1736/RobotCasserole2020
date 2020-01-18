package frc.robot.ControlPanel;

import frc.robot.ControlPanel.CasseroleColorSensor;
import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.ControlPanel.ControlPanelManipulator;


public class ControlPanelStateMachine{

    /* Singelton Stuff */
    private static ControlPanelStateMachine instance = null;
    
    
    CasseroleColorSensor colorSensor;
    

    public static synchronized ControlPanelStateMachine getInstance() {
        if(instance == null)
        instance = new ControlPanelStateMachine();
        return instance;
    }

    int colorOnWheel = CasseroleColorSensor.getInstance().getControlPanelColor();

    ControlPanelColor gameDataColor = ControlPanelColor.kUNKNOWN;

    private ControlPanelStateMachine(){
        //System.out.println("The color on the wheel is "+ColorOnWheel);
        colorSensor = CasseroleColorSensor.getInstance();

    }

    public void parseGameData(){

        String gameData;
        gameData = DriverStation.getInstance().getGameSpecificMessage();

        if(gameData.length() > 0){
            if(gameData.charAt(0) == 'R'){
                gameDataColor = ControlPanelColor.kRED;
            }
            if(gameData.charAt(0) == 'G'){
                gameDataColor = ControlPanelColor.kGREEN;
            }
            if(gameData.charAt(0) == 'B'){
                gameDataColor = ControlPanelColor.kBLUE;
            }
            if(gameData.charAt(0) == 'Y'){
                gameDataColor = ControlPanelColor.kYELLOW;
            }
        } else {
            gameDataColor = ControlPanelColor.kUNKNOWN;
        }
    }

    public void setRotateToColorDesired(boolean rotateToColorCmd_in){

    }
    
    
    public boolean degreesToTurn(int colorOnWheelList,int colorGotten){
        boolean Rotation = true;
        for(int i = 0; i < 3; i++){



        }
        return true;
    }

    //change Tur to Turn if we decide on this function.
    public int degreesToTur(ControlPanelColor colorOnWheel, String gameData){
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
        }else{ //kUNKNOWN
            //Maybe adjust this. I wasn't sure why we would rotate 10 degrees but thats what was already in code so...
            return 10;
        }
        
        int rotateCmd=desiredRotation-(colorOnWheel.value*45);

        //checks if there is a 135 degree rotation. If so sets it to it 45 degrees in the opposite direction
        if(Math.abs(rotateCmd)>90){
            rotateCmd=(rotateCmd/-3);
        }

        return rotateCmd;
    }

        //TODO - user will pass in true if they want the rotate-to-color cycle to start and run, or false if they want to stop the cycle.

    public void setRotateStage2Desired(boolean rotateStage2Cmd_in){
        //TODO - user will pass in true if they want the rotate-to-color cycle to start and run, or false if they want to stop the cycle.

    }

    public void update(){
        parseGameData();
        colorSensor.update();

    }


}