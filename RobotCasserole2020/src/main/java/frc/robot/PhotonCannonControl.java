package frc.robot;

import edu.wpi.first.wpilibj.DigitalOutput;

public class PhotonCannonControl {
    private static PhotonCannonControl inst = null;
    public static synchronized PhotonCannonControl getInstance() {
        if (inst==null){
            inst = new PhotonCannonControl();
        }
        return inst;
    }

    DigitalOutput photonRelayOutput;

    private PhotonCannonControl(){
        photonRelayOutput = new DigitalOutput(RobotConstants.PHOTON_CANNON_PORT);
    }

    public void setPhotonCannonState(boolean enabled){
        photonRelayOutput.set(enabled);
    }
    

    
}