package frc.robot;

public class PWMSpeedController{
    double arduino = 0;
    int prime = 1;
    

    
    
    public PWMSpeedController(){
        
    }
    public void update(){
        prime++;
        if (prime >= 500){
            arduino = -1;
            prime=0;
        }
    }
}
   
