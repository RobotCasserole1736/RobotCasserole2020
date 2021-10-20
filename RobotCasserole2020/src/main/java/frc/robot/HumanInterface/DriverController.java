package frc.robot.HumanInterface;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import frc.lib.Calibration.Calibration;
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

    LinearFilter fwdRevPulseFilter;
    LinearFilter rotPulseFilter;

    Calibration fwdRevPfFFCal;
    Calibration rotPfFFCal;
    Calibration fwdRevPfSkewCal;
    Calibration rotPfSkewCal;
    Calibration maxRotSpdCal;
    Calibration useCurvatureDriveCal;
    Calibration usePulseFilter;

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

        fwdRevPfFFCal   = new Calibration("Driver FwdRev Joy Filter Factor", 0.3);
        rotPfFFCal      = new Calibration("Driver Rot Joy Filter Factor", 0.2);
        fwdRevPfSkewCal = new Calibration("Driver FwdRev Joy Map Skew", 4.0);
        rotPfSkewCal    = new Calibration("Driver Rot Joy Map Skew", 1.5);
        maxRotSpdCal    = new Calibration("Driver Max Rot Speed", 1.0);
        useCurvatureDriveCal = new Calibration("Driver Use Curvature Drive", 1.0);
        usePulseFilter = new Calibration("Driver Use Joy Filters", 0.0);
        
        fwdRevPulseFilter = LinearFilter.singlePoleIIR(fwdRevPfFFCal.get(), 0.02);
        rotPulseFilter    = LinearFilter.singlePoleIIR(rotPfFFCal.get(), 0.02);
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

        //Raw driver commands come right from the joystick
        var fwdRevCmdRaw = -1.0*driverController.getY(GenericHID.Hand.kLeft);
        var rotCmdRaw = -1.0*driverController.getX(GenericHID.Hand.kRight);
        
        // Prefilt commands come from deadzones, scaling, and skewing operations
        var fwdRevCmdPrefilt =  Utils.ctrlAxisScale(fwdRevCmdRaw, fwdRevPfSkewCal.get(), 0.10);
        var rotCmdPrefilt =  Utils.ctrlAxisScale(rotCmdRaw, rotPfSkewCal.get(), 0.01);

        //Flips which side is the front and back in regards to driving
        if(reverseModeCmd){
            fwdRevCmdPrefilt *= -1.0;
        }

        if(usePulseFilter.get() > 0.5){
            //Re-init filters on calibration change
            if(fwdRevPfFFCal.isChanged()){
                fwdRevPulseFilter = LinearFilter.singlePoleIIR(fwdRevPfFFCal.get(), 0.02);
                fwdRevPfFFCal.acknowledgeValUpdate();
            }
            if(rotPfFFCal.isChanged()){
                rotPulseFilter    = LinearFilter.singlePoleIIR(rotPfFFCal.get(), 0.02);
                rotPfFFCal.acknowledgeValUpdate();
            }

            // Apply Filter
            if(Math.abs(fwdRevCmd - fwdRevCmdPrefilt) > 1.2){
                fwdRevPulseFilter.reset();
            }
            if(Math.abs(rotCmd - rotCmdPrefilt) > 1.2){
                rotPulseFilter.reset();
            }

            fwdRevCmd = fwdRevPulseFilter.calculate(fwdRevCmdPrefilt);
            rotCmd = rotPulseFilter.calculate(rotCmdPrefilt);
        } else {
            //No filtering, just pass through
            fwdRevCmd = fwdRevCmdPrefilt;
            rotCmd = rotCmdPrefilt;
        }

        if(useCurvatureDriveCal.get() > 0.5){
            //Cheesy Drive - Use Fwd/Rev command to scale rotCmd (effectively making rotation control curvature)
            if(Math.abs(fwdRevCmd) > 0.1){
                rotCmd = Math.abs(fwdRevCmd) * rotCmd * 1.0 * maxRotSpdCal.get(); //Modifiy scalar for sensitivity
            } else {
                rotCmd = rotCmdPrefilt;
            }
        }     
        rotCmd *= maxRotSpdCal.get();

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