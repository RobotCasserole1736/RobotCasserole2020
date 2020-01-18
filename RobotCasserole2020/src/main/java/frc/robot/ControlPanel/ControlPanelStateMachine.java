package frc.robot.ControlPanel;

import frc.robot.ControlPanel.CasseroleColorSensor;
import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.ControlPanel.ControlPanelManipulator;


public class ControlPanelStateMachine{

    /* Singelton Stuff */
    private static ControlPanelStateMachine instance = null;
    
    
    
    
    

    public static synchronized ControlPanelStateMachine getInstance() {
        if(instance == null)
        instance = new ControlPanelStateMachine();
        return instance;
    }

    int colorOnWheel = CasseroleColorSensor.getInstance().getControlPanelColor();

    private ControlPanelStateMachine(){
        //System.out.println("The color on the wheel is "+ColorOnWheel);

        String gameData;
        gameData = DriverStation.getInstance().getGameSpecificMessage();
        char colorDesired = gameData.charAt(0);
        int[] colorOnWheelList={0,1,2,3};

        int colorGotten;

        if(gameData.charAt(0) == 'R'){
            colorGotten = 0;
        }
        if(gameData.charAt(0) == 'G'){
            colorGotten = 1;
        }
        if(gameData.charAt(0) == 'B'){
            colorGotten = 2;
        }
        if(gameData.charAt(0) == 'Y'){
            colorGotten = 3;
        }

    }

    public void setRotateToColorDesired(boolean rotateToColorCmd_in){
        String gameData;
        gameData = DriverStation.getInstance().getGameSpecificMessage();
        if(gameData.length() > 0){
            switch (gameData.charAt(0))
            {
              case 'B' :
                //Blue case code
                break;
              case 'G' :
                //Green case code
                break;
              case 'R' :
                //Red case code
                break;
              case 'Y' :
                //Yellow case code
                break;
              default :
                //This is corrupt data
                break;
            }
          } else {
            //Code for no data received yet
          }
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
        String desiredColor=String.valueOf(gameData.charAt(0));
        int desiredRotation;
        if(desiredColor.equals("R")){
            desiredRotation=0;
        }else if(desiredColor.equals("G")){
            desiredRotation=45;
        }else if(desiredColor.equals("B")){
            desiredRotation=90;
        }else if(desiredColor.equals("Y")){
            desiredRotation=135;
        }else{
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


}