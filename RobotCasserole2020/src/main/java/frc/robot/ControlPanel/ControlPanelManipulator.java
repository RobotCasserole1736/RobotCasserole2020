package frc.robot.ControlPanel;


public class ControlPanelManipulator{

    /* Singelton Stuff */
    private static ControlPanelManipulator instance = null;
    public static synchronized ControlPanelManipulator getInstance() {
        if(instance == null)
        instance = new ControlPanelManipulator();
        return instance;
    }

    boolean rotationComplete = false;
    double desiredRotation_deg = 0.0;



    private ControlPanelManipulator(){
        //TODO
    }

    public void update(){
        //TODO
    }

    public void sendRotationCommand(double desRotation_deg_in){
        rotationComplete = false;
        desiredRotation_deg += desRotation_deg_in; 
    }

    public boolean isRotationCompleted(){
        return rotationComplete;
    }

    public void stopRotation(){
        //TODO
    }



}
