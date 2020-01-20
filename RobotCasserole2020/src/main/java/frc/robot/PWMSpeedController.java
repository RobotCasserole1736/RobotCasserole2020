package frc.robot;

public class PWMSpeedController{
    double arduino = 0;
    int prime = -1;
    

    
    
    public PWMSpeedController(){
        
    }
    public void update(){
        prime++;
        if (prime >= 1500){
            arduino = 0;
            prime=-1;
        }
    }
}
   
