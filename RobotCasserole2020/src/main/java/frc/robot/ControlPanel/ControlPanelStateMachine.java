package frc.robot.ControlPanel;

import frc.robot.ControlPanel.CasseroleColorSensor;
import edu.wpi.first.wpilibj.DriverStation;

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

    public int degreesToTurn(int colorOnWheel, String gameData){
        if(colorOnWheel == 0){
            if(gameData.charAt(0) == 'B'){
                //move motor 90 degrees clockwise
            } else if(gameData.charAt(0) == 'G'){
                //move motor 45 degrees clockwise
            }else if(gameData.charAt(0) == 'Y'){
                //move motor 45 degrees counter-clockwise
            }
        } else if(colorOnWheel == 1){
            if(gameData.charAt(0) == 'Y'){
                //move motor 90 degrees clockwise
            } else if(gameData.charAt(0) == 'B'){
                //move motor 45 degrees clockwise
            }else if(gameData.charAt(0) == 'R'){
                //move motor 45 degrees counter-clockwise
            }
        } else if(colorOnWheel == 2){
            if(gameData.charAt(0) == 'R'){
                //move motor 90 degrees clockwise
            } else if(gameData.charAt(0) == 'Y'){
                //move motor 45 degrees clockwise
            }else if(gameData.charAt(0) == 'G'){
                //move motor 45 degrees counter-clockwise
            }
        } else if(colorOnWheel == 3){
            if(gameData.charAt(0) == 'Y'){
                //move motor 90 degrees clockwise
            } else if(gameData.charAt(0) == 'R'){
                //move motor 45 degrees clockwise
            }else if(gameData.charAt(0) == 'B'){
                //move motor 45 degrees counter-clockwise
            }
        } else {
            return 10; //10 is the number of degrees to turn
        }
        return 1; //delete when function is finished.
    }
        //TODO - user will pass in true if they want the rotate-to-color cycle to start and run, or false if they want to stop the cycle.

    public void setRotateStage2Desired(boolean rotateStage2Cmd_in){
        //TODO - user will pass in true if they want the rotate-to-color cycle to start and run, or false if they want to stop the cycle.

    }


}