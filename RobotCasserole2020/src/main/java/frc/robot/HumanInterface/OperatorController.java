package frc.robot.HumanInterface;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;

/*
 *******************************************************************************************
 * Copyright (C) 2020 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *   find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *   you have going on right now! We'd love to be able to help out! Shoot us 
 *   any questions you may have, all our contact info should be on our website
 *   (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *   Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *   if you would consider donating to our club to help further STEM education.
 */

public class OperatorController {
    XboxController operaterController;
    private static OperatorController instance = null;
    
    public static synchronized OperatorController getInstance() {
        if(instance == null)
        instance = new OperatorController();
        return instance;
    }

    public boolean shootCmd = false;
    public boolean prepToShootCmd = false;
    public boolean intakeDesired = false;
    public boolean unjamCmd = false;
    public boolean ejectDesired = false;
    public boolean photonCannonCmd = false;
    public boolean climbEnabledCmd = false;
    public double  climbSpeedCmd = 0.0;
    public boolean ctrlPanelThreeRotationsDesired = false;
    public boolean ctrlPanelSeekToColorDesired = false;

    Signal shootCmdSig;
    Signal prepToShootCmdSig;
    Signal intakeDesiredSig;
    Signal unjamCmdSig;
    Signal ejectDesiredSig;
    Signal photonCannonCmdSig;
    Signal climbEnabledCmdSig;
    Signal climbSpeedCmdSig;
    Signal ctrlPanelThreeRotationsDesiredSig;
    Signal ctrlPanelSeekToColorDesiredSig;

    // This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
    private OperatorController() {
        operaterController= new XboxController(1);

        shootCmdSig = new Signal("Operator Controller Shoot Command", "bool");
        prepToShootCmdSig = new Signal("Operator Controller Prep to Shoot Command", "bool");
        intakeDesiredSig = new Signal("Operator Controller Intake Desired Command", "bool");
        unjamCmdSig = new Signal("Operator Controller Unjam Command", "bool");
        ejectDesiredSig = new Signal("Operator Controller Eject Desired Command", "bool");
        photonCannonCmdSig = new Signal("Operator Controller Photon Cannon Command", "bool");
        climbEnabledCmdSig = new Signal("Operator Controller Climb Enable Command", "bool");
        climbSpeedCmdSig = new Signal("Operator Controller Climb Speed Command", "cmd");
        ctrlPanelThreeRotationsDesiredSig = new Signal("Operator Controller Ctrl Panel Three Rotations Command", "bool");
        ctrlPanelSeekToColorDesiredSig = new Signal("Operator Controller Ctrl Panel Seek To Color Command", "bool");
        
    }

    public void update(){
        shootCmd = (operaterController.getTriggerAxis(Hand.kRight) > 0.2);
        prepToShootCmd = operaterController.getBumper(Hand.kLeft);
        intakeDesired = (operaterController.getTriggerAxis(Hand.kLeft) > 0.2);
        unjamCmd = operaterController.getBumper(Hand.kLeft);
        ejectDesired = operaterController.getBackButton(); 
        photonCannonCmd = operaterController.getAButton();
        climbEnabledCmd = operaterController.getStartButton();
        climbSpeedCmd = operaterController.getY(GenericHID.Hand.kLeft);
        ctrlPanelThreeRotationsDesired = operaterController.getYButton();
        ctrlPanelSeekToColorDesired = operaterController.getBButton();

        double time_in_ms = LoopTiming.getInstance().getLoopStartTimeSec()*1000;
        shootCmdSig.addSample(time_in_ms, shootCmd);
        prepToShootCmdSig.addSample(time_in_ms, prepToShootCmd);
        intakeDesiredSig.addSample(time_in_ms, intakeDesired);
        unjamCmdSig.addSample(time_in_ms, unjamCmd);
        ejectDesiredSig.addSample(time_in_ms, ejectDesired);
        photonCannonCmdSig.addSample(time_in_ms, photonCannonCmd);
        climbEnabledCmdSig.addSample(time_in_ms, climbEnabledCmd);
        climbSpeedCmdSig.addSample(time_in_ms, climbSpeedCmd);
        ctrlPanelThreeRotationsDesiredSig.addSample(time_in_ms, ctrlPanelThreeRotationsDesired);
        ctrlPanelSeekToColorDesiredSig.addSample(time_in_ms, ctrlPanelSeekToColorDesired);
    }
    
    public boolean getShootCmd(){
        return shootCmd;
    }
    public boolean getPrepToShootCmd() {
        return prepToShootCmd; 
    }     
    public boolean getIntakeDesired(){
        return intakeDesired;
    }
    public boolean getUnjamCmd(){
        return unjamCmd;
    }   
    public boolean getEjectDesired(){
         return ejectDesired;
    }
    public boolean getPhotonCannonCmd(){
        return photonCannonCmd;
    }
    public boolean getClimbEnableCmd(){
        return climbEnabledCmd;
    }
    public double getClimbSpeedCmd(){
        return climbSpeedCmd; 
    }
    public boolean getControlPanelThreeRotationsDesired(){
        return ctrlPanelThreeRotationsDesired; 
    }
    public boolean getControlPanelSeekToColorDesired(){
        return ctrlPanelSeekToColorDesired; 
    }

    //Extras
    public boolean createSound(){
        return operaterController.getStickButton(Hand.kLeft);
    }

    public void rumble(double strength) {
        operaterController.setRumble(RumbleType.kLeftRumble, strength);
        operaterController.setRumble(RumbleType.kRightRumble, strength);
    }

    

}
