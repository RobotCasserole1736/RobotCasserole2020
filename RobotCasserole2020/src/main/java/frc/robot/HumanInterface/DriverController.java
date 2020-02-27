package frc.robot.HumanInterface;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import frc.robot.Drivetrain.Utils;
import edu.wpi.first.wpilibj.GenericHID;

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

public class DriverController {
    XboxController driverController;
    private static DriverController instance = null;
    boolean compressorEnableReq = true;
    boolean compressorDisableReq = false;

    double fwdRevCmd = 0;
    double rotCmd = 0;
    boolean autoAlignCmd = false;
    boolean autoAlignAndShootCmd = false;
    boolean autoAlignAndShootCloseCmd = false;
    boolean snailModeCmd = true;
    boolean reverseModeCmd = false;

    Signal fwdRevCmdSig;
    Signal rotCmdSig;
    Signal autoAlignAndShootCmdSig;
    Signal autoAlignAndShootCloseCmdSig;
    Signal autoAlignCmdSig;
    Signal snailModeCmdSig;
    Signal reverseModeSig;

    
    public static synchronized DriverController getInstance() {
		if(instance == null)
		    instance = new DriverController();
		return instance;
	}

	// This is the private constructor that will be called once by getInstance() and it should instantiate anything that will be required by the class
	private DriverController() {
        driverController = new XboxController(0);

        fwdRevCmdSig    = new Signal("Driver FwdRev Command", "cmd");
        rotCmdSig       = new Signal("Driver Rotate Command", "cmd");
        autoAlignAndShootCmdSig = new Signal("Driver Auto Align and Shoot Command", "bool");
        autoAlignAndShootCloseCmdSig =  new Signal("Driver Auto Align Close and Shoot Command", "bool");
        autoAlignCmdSig = new Signal("Driver Auto Align Only Command", "bool");
        snailModeCmdSig = new Signal("Driver Snail Mode Command", "bool");
        reverseModeSig  = new Signal("Driver Flip Front/Back Command", "bool");
    }

    public void update(){
        if(driverController.getStartButton()){
            compressorEnableReq = true;
            compressorDisableReq = false;
        } else if(driverController.getBackButton()) {
            compressorEnableReq = false;
            compressorDisableReq = true;
        } else {
            compressorEnableReq = false;
            compressorDisableReq = false;
        }

        reverseModeCmd = driverController.getBumper(Hand.kLeft);
        fwdRevCmd =  Utils.ctrlAxisScale(-1.0*driverController.getY(GenericHID.Hand.kLeft), 4.0, 0.10);
        rotCmd =  Utils.ctrlAxisScale(-1.0*driverController.getX(GenericHID.Hand.kRight), 2.5, 0.10);

        //Flips which side is the front and back in regards to driving
        if(reverseModeCmd){
            fwdRevCmd *= -1.0;
        }
        
        autoAlignCmd = driverController.getXButton();
        autoAlignAndShootCmd = (driverController.getTriggerAxis(Hand.kRight) > 0.2);
        autoAlignAndShootCloseCmd = (driverController.getTriggerAxis(Hand.kLeft) > 0.2);
        snailModeCmd = !driverController.getBumper(Hand.kRight);

        double time_in_ms = LoopTiming.getInstance().getLoopStartTimeSec()*1000;
        fwdRevCmdSig.addSample(time_in_ms, fwdRevCmd);
        rotCmdSig.addSample(time_in_ms, rotCmd);      
        autoAlignCmdSig.addSample(time_in_ms, autoAlignCmd);
        autoAlignAndShootCmdSig.addSample(time_in_ms, autoAlignAndShootCmd);
        autoAlignAndShootCloseCmdSig.addSample(time_in_ms, autoAlignAndShootCloseCmd);
        snailModeCmdSig.addSample(time_in_ms, snailModeCmd);
        reverseModeSig.addSample(time_in_ms, reverseModeCmd);
    }

    public boolean getCompressorDisableReq() {
        return this.compressorDisableReq;
    }

    public boolean getCompressorEnableReq() {
        return this.compressorEnableReq;
    }
    /**
     * Get the driver-commanded forward/reverse speed
     * @return 1.0 for full forward, -1.0 for full reverse
     */
    public double getFwdRevCmd(){
        return fwdRevCmd; 
    }

    /**
     * Get the driver-commanded rotation
     * @return -1.0 for clockwise, 1.0 for counter-clockwise
     */
    public double getRotateCmd(){
        return rotCmd; 
    }

    public boolean getAutoAlignAndShootCmd(){
        return autoAlignAndShootCmd; 
    }

    public boolean getAutoAlignAndShootCloseCmd(){
        return autoAlignAndShootCloseCmd;
    }

    public boolean getAutoAlignCmd(){
        return autoAlignCmd; 
    }
    
    public boolean getPhotonCannonInput(){
        return driverController.getYButton();
    }

    public boolean getSnailModeDesired(){
        return snailModeCmd;
    }

    public boolean getReverseModeDesired(){
        return reverseModeCmd;
    }

    void rumble(double strength) {
        driverController.setRumble(RumbleType.kLeftRumble, strength);
        driverController.setRumble(RumbleType.kRightRumble, strength);
    }


}