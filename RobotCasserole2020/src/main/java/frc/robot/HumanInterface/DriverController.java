package frc.robot.HumanInterface;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.filters.Filter;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import frc.robot.Drivetrain.Utils;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.LinearFilter;

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
    boolean loadingToTrenchCmd = false;
    boolean trenchToLoadingCmd = false;
    boolean autoAlignAndShootCmd = false;
    boolean turn180DegCmd = false;
    
    boolean autoAlignAndShootCloseCmd = false;
    boolean snailModeCmd = true;
    boolean reverseModeCmd = false;

    Signal fwdRevCmdSig;
    Signal rotCmdSig;
    Signal autoAlignAndShootCmdSig;
    Signal autoAlignAndShootCloseCmdSig;
    Signal autoAlignCmdSig;
    Signal loadingToTrenchCmdSig;
    Signal trenchToLoadingCmdSig;
    Signal snailModeCmdSig;
    Signal reverseModeSig;
    Signal turn180DegCmdSig;

    private final boolean USE_PULSE_FILTER = true;
    LinearFilter fwdRevPulseFilter = LinearFilter.singlePoleIIR(0.3, 0.02);
    LinearFilter rotPulseFilter = LinearFilter.singlePoleIIR(0.2, 0.02);

    
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
        loadingToTrenchCmdSig = new Signal("Driver Loading To Trench Command", "bool");
        trenchToLoadingCmdSig = new Signal("Driver Trench To Loading Align Only Command", "bool");
        turn180DegCmdSig = new Signal("Driver Turn 180 Deg Command", "bool");
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
        
        var fwdRevCmdPrefilt =  Utils.ctrlAxisScale(-1.0*driverController.getY(GenericHID.Hand.kLeft), 4.0, 0.10);
        var rotCmdPrefilt =  Utils.ctrlAxisScale(-1.0*driverController.getX(GenericHID.Hand.kRight), 1.5, 0.01) * 0.85;

        //Flips which side is the front and back in regards to driving
        if(reverseModeCmd){
            fwdRevCmdPrefilt *= -1.0;
        }

        if(USE_PULSE_FILTER){
            if(Math.abs(fwdRevCmd - fwdRevCmdPrefilt) > 1.2){
                fwdRevPulseFilter.reset();
            }

            if(Math.abs(rotCmd - rotCmdPrefilt) > 1.2){
                rotPulseFilter.reset();
            }

            fwdRevCmd = fwdRevPulseFilter.calculate(fwdRevCmdPrefilt);
            rotCmd = rotPulseFilter.calculate(rotCmdPrefilt);
        } else {
            fwdRevCmd = fwdRevCmdPrefilt;
            rotCmd = rotCmdPrefilt;
        }

        
        autoAlignCmd = driverController.getXButton();
        loadingToTrenchCmd = driverController.getAButton();
        trenchToLoadingCmd = driverController.getBButton();
        turn180DegCmd = driverController.getYButton();
        autoAlignAndShootCmd = (driverController.getTriggerAxis(Hand.kRight) > 0.2);
        autoAlignAndShootCloseCmd = (driverController.getTriggerAxis(Hand.kLeft) > 0.2);
        snailModeCmd = !driverController.getBumper(Hand.kRight);

        double time_in_ms = LoopTiming.getInstance().getLoopStartTimeSec()*1000;
        fwdRevCmdSig.addSample(time_in_ms, fwdRevCmd);
        rotCmdSig.addSample(time_in_ms, rotCmd);      
        autoAlignCmdSig.addSample(time_in_ms, autoAlignCmd);
        autoAlignAndShootCmdSig.addSample(time_in_ms, autoAlignAndShootCmd);
        autoAlignAndShootCloseCmdSig.addSample(time_in_ms, autoAlignAndShootCloseCmd);
        loadingToTrenchCmdSig.addSample(time_in_ms, loadingToTrenchCmd);
        trenchToLoadingCmdSig.addSample(time_in_ms, trenchToLoadingCmd);
        turn180DegCmdSig.addSample(time_in_ms, turn180DegCmd);
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

    public boolean getLoadingToTrenchCmd(){
        return loadingToTrenchCmd;
    }

    public boolean getTrenchToLoadingCmd(){
        return trenchToLoadingCmd;
    }

    public boolean getTurn180DegCmd(){
        return turn180DegCmd;
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