package frc.robot.ControlPanel;


class ControlPanelStateMachine{

    /* Singelton Stuff */
    private static ControlPanelStateMachine instance = null;
    public static synchronized ControlPanelStateMachine getInstance() {
        if(instance == null)
        instance = new ControlPanelStateMachine();
        return instance;
    }

    private ControlPanelStateMachine(){
        //TODO
    }

    public void update(){
        //TODO
    }

    public void setRotateToColorDesired(boolean rotateToColorCmd_in){
        //TODO - user will pass in true if they want the rotate-to-color cycle to start and run, or false if they want to stop the cycle.
    }

    public void setRotateStage2Desired(boolean rotateStage2Cmd_in){
        //TODO - user will pass in true if they want the rotate-to-color cycle to start and run, or false if they want to stop the cycle.

    }


}