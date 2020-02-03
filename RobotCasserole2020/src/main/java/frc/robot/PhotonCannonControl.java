package frc.robot;

import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.Timer;
import frc.lib.DataServer.Signal;

public class PhotonCannonControl {
    
    private static PhotonCannonControl inst = null;
    public static synchronized PhotonCannonControl getInstance() {
        if (inst==null){
            inst = new PhotonCannonControl();
        }
        return inst;
    }

    boolean prevCannonState = false;
    boolean curCannonState;
    DigitalOutput photonRelayOutput;

    Signal photonCannonSignal;

    private PhotonCannonControl(){
        photonRelayOutput = new DigitalOutput(RobotConstants.PHOTON_CANNON_PORT);
        photonCannonSignal = new Signal("Photon Cannon State", "bool");
    }

    public void setPhotonCannonState(boolean enabled){
        curCannonState = enabled;
    }
    
    public void update(){
        photonRelayOutput.set(!curCannonState);
        prevCannonState = curCannonState;

        double sample_time_ms = Timer.getFPGATimestamp()*1000;
        photonCannonSignal.addSample(sample_time_ms, curCannonState);
    }

    
}