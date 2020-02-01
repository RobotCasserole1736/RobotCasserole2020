/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2020 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import frc.lib.Calibration.CalWrangler;
import frc.lib.WebServer.CasseroleDriverView;
import frc.lib.DataServer.CasseroleDataServer;
import frc.lib.Util.CrashTracker;
import frc.lib.WebServer.CasseroleWebServer;
import frc.lib.DataServer.Signal;
import frc.robot.Drivetrain.Drivetrain;
import frc.robot.HumanInterface.DriverController;
import frc.robot.HumanInterface.OperatorController;
import frc.robot.ShooterControl.ShooterControl;
import frc.robot.ShooterControl.ShooterControl.ShooterCtrlMode;
import frc.robot.VisionProc.CasseroleVision;
import frc.robot.VisionProc.VisionCamera;
import frc.robot.Autonomous.Autonomous;
import frc.robot.BallHandling.BallDistanceSensor;
import frc.robot.BallHandling.Hopper;
import frc.robot.BallHandling.IntakeControl;
import edu.wpi.first.wpilibj.RobotController;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;
import frc.robot.ControlPanel.ControlPanelStateMachine;
import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.LEDController.LEDPatterns;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  //Website utilities
  CasseroleWebServer webserver;
  CalWrangler wrangler;
  CasseroleDataServer dataServer;
  LoopTiming loopTiming;
  PowerDistributionPanel pdp;
  CasseroleRIOLoadMonitor loadMon;

  //Top level telemetry signals
  Signal rioDSSampLoadSig;
  Signal rioDSLogQueueLenSig;
  Signal rioCurrDrawLoadSig;
  Signal rioBattVoltLoadSig;
  Signal rioIsBrownoutSig;
  Signal rioCANBusUsagePctSig;

  //Autonomous Control Utilities
  Autonomous auto;

  //Sensors and Cameras and stuff, oh my!
  VisionCamera cam;
  PhotonCannonControl photonCannon;
  VisionLEDRingControl eyeOfVeganSauron;

  //Subsystems
  Drivetrain drivetrain;
  ShooterControl shooterCtrl;
  IntakeControl intakeCtrl;
  Hopper hopper;
  Climber climber;
  PneumaticsControl thbbtbbtbbtbbt;
  ControlPanelStateMachine ctrlPanel;
  LEDController ledController;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~ Robot Init
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  @Override
  public void robotInit() {
    
    CrashTracker.logRobotInit();

    /* Init website utilties */
    webserver = new CasseroleWebServer();
    wrangler = new CalWrangler();
    dataServer = CasseroleDataServer.getInstance();
    cam = CasseroleVision.getInstance();
    pdp = CasserolePDP.getInstance();
    loadMon= new CasseroleRIOLoadMonitor();

    /* Init local telemetry signals */
    rioDSSampLoadSig = new Signal("Dataserver Stored Samples", "count"); 
    rioCurrDrawLoadSig = new Signal("Battery Current Draw", "A");
    rioBattVoltLoadSig = new Signal("Battery Voltage", "V");
    rioDSLogQueueLenSig = new Signal("Dataserver File Logger Queue Length", "count");
    rioIsBrownoutSig = new Signal("Robot Brownout", "bool");
    rioCANBusUsagePctSig = new Signal("Robot CAN Bus Utilization", "pct");

    thbbtbbtbbtbbt = PneumaticsControl.getInstance();
    eyeOfVeganSauron = VisionLEDRingControl.getInstance();
    photonCannon = PhotonCannonControl.getInstance();
    ledController = LEDController.getInstance();


    OperatorController.getInstance();
    DriverController.getInstance();
    ctrlPanel = ControlPanelStateMachine.getInstance();

    shooterCtrl = ShooterControl.getInstance();

    drivetrain = Drivetrain.getInstance();

    auto = Autonomous.getInstance();

    intakeCtrl = IntakeControl.getInstance();

    hopper = Hopper.getInstance();

    climber= Climber.getInstance();

    loopTiming = LoopTiming.getInstance();

    ControlPanelStateMachine.getInstance();

    /* Website Setup */
    initDriverView();

    dataServer.startServer();
    webserver.startServer();
    
  }
  
  public void telemetryUpdate(){
    double sampleTimeMs = loopTiming.getLoopStartTimeSec()*1000.0;

    rioDSSampLoadSig.addSample(sampleTimeMs, dataServer.getTotalStoredSamples());
    rioCurrDrawLoadSig.addSample(sampleTimeMs, pdp.getTotalCurrent());
    rioBattVoltLoadSig.addSample(sampleTimeMs, pdp.getVoltage());  
    rioDSLogQueueLenSig.addSample(sampleTimeMs, dataServer.logger.getSampleQueueLength());
    rioIsBrownoutSig.addSample(sampleTimeMs, RobotController.isBrownedOut());
    rioCANBusUsagePctSig.addSample(sampleTimeMs, RobotController.getCANStatus().percentBusUtilization);
  }
    
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~ DISABLED MODE
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  @Override
  public void disabledInit() {
    CrashTracker.logDisabledInit();
    dataServer.logger.stopLogging();
    auto.reset();

  }

  @Override
  public void disabledPeriodic() {
    loopTiming.markLoopStart();
    CrashTracker.logDisabledPeriodic();


    BallDistanceSensor.getInstance().update();
    ledController.setPattern(LEDPatterns.Pattern0);

    thbbtbbtbbtbbt.update();
    eyeOfVeganSauron.setLEDRingState(false);
    photonCannon.setPhotonCannonState(false);
    photonCannon.update();
    cam.update();
    
    auto.sampleDashboardSelector();

    ctrlPanel.update();

    //shooterCtrl.update();
    intakeCtrl.update();

    drivetrain.setOpenLoopCmd(0, 0);
    drivetrain.updateGains(false);
    drivetrain.update();

    updateDriverView();
    telemetryUpdate();
    loopTiming.markLoopEnd();
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~ AUTONOMOUS MODE
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  @Override
  public void autonomousInit() {
    CrashTracker.logAutoInit();
    dataServer.logger.startLoggingAuto();
    auto.sampleDashboardSelector();
    auto.startSequencer(); //Actually trigger the start of whatever autonomous routine we're doing
  }

  @Override
  public void autonomousPeriodic() {
    loopTiming.markLoopStart();
    CrashTracker.logAutoPeriodic();

    ledController.setPattern(LEDPatterns.Pattern2);

    thbbtbbtbbtbbt.update();
    eyeOfVeganSauron.setLEDRingState(true);
    photonCannon.setPhotonCannonState(false);
    photonCannon.update();
    cam.update();

    auto.update();

    ctrlPanel.update();
    //shooterCtrl.update();
    hopper.update();
    intakeCtrl.update();
    climber.update();

    drivetrain.update();

    ledController.update();

    updateDriverView();
    telemetryUpdate();

    // put all code before this
    loopTiming.markLoopEnd();
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~ TELEOP MODE
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  @Override
  public void teleopInit() {
    CrashTracker.logTeleopInit();
    dataServer.logger.startLoggingTeleop();
  }

  @Override
  public void teleopPeriodic() {
    loopTiming.markLoopStart();
    CrashTracker.logTeleopPeriodic();

    ledController.setPattern(LEDPatterns.Pattern1);

    //Based on operator commands, change which photon source we use.
    if(OperatorController.getInstance().flashlightCmd()){
      photonCannon.setPhotonCannonState(true);
      eyeOfVeganSauron.setLEDRingState(false);
    } else {
      photonCannon.setPhotonCannonState(false);
      eyeOfVeganSauron.setLEDRingState(true);
    }
    photonCannon.update();
    cam.update();


    thbbtbbtbbtbbt.update();

    auto.sampleOperatorCommands();
    auto.update();

    //shooterCtrl.update();
    intakeCtrl.update();
    climber.update();
    hopper.update();
    ctrlPanel.update();
    ledController.update();

    if(auto.isActive()){
      //Nothing to do, expect that auto sequencer will provide drivetrain comands
    } else {
      //Driver control in manual
      drivetrain.setOpenLoopCmd(DriverController.getInstance().getFwdRevCmd(), 
                                DriverController.getInstance().getRotateCmd());
    }

    drivetrain.update();

    updateDriverView();
    telemetryUpdate();

    // put all code before this 
    loopTiming.markLoopEnd();
    
  }


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~ UTILITIES
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  private void initDriverView(){
    CasseroleDriverView.newDial("System Press (PSI)", 0, 150, 10, 90, 130);
    CasseroleDriverView.newDial("Shooter Speed (RPM)", 0, 6000, 600, 4500, 5700);
    CasseroleDriverView.newDial("Robot Speed (fps)", 0, 20, 2, 5, 15);
    CasseroleDriverView.newBoolean("Vision Camera Fault", "red");
    CasseroleDriverView.newBoolean("Vision Target Visible", "green");
    CasseroleDriverView.newBoolean("Climber Lower SW Fault", "red");
    CasseroleDriverView.newBoolean("Climber Upper SW Fault", "red");
    CasseroleDriverView.newBoolean("Shooter Spoolup", "yellow");
    CasseroleDriverView.newSoundWidget("High Ground Acqd", "./highground.mp3");
    CasseroleDriverView.newAutoSelector("Action", Autonomous.ACTION_MODES);
		CasseroleDriverView.newAutoSelector("Delay", Autonomous.DELAY_OPTIONS);
    CasseroleDriverView.newWebcam("cam1", "http://10.17.36.10:1181/stream.mjpg", 50, 75);
    CasseroleDriverView.newWebcam("cam2", "http://10.17.36.10:1182/stream.mjpg", 50, 75);

  }

  public void updateDriverView(){
    CasseroleDriverView.setDialValue("System Press (PSI)", thbbtbbtbbtbbt.getPressure());
    CasseroleDriverView.setDialValue("Shooter Speed (RPM)", shooterCtrl.getSpeedRPM());
    CasseroleDriverView.setDialValue("Robot Speed (fps)", drivetrain.getRobotSpeedfps());
    CasseroleDriverView.setBoolean("Vision Camera Fault", !cam.isVisionOnline());
    CasseroleDriverView.setBoolean("Vision Target Visible", cam.isTgtVisible());
    CasseroleDriverView.setBoolean("Climber Lower SW Fault", climber.isLowerLimitSwitchFaulted());
    CasseroleDriverView.setBoolean("Climber Upper SW Fault", climber.isUpperLimitSwitchFaulted());
    CasseroleDriverView.setBoolean("Shooter Spoolup", (shooterCtrl.getShooterCtrlMode() == ShooterCtrlMode.SpoolUp));
    CasseroleDriverView.setSoundWidget("High Ground Acqd",false); //TODO
  }


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~ TEST MODE
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  @Override
  public void testInit() {
    
  }

  @Override
  public void testPeriodic() {
    loopTiming.markLoopStart();
    
    telemetryUpdate();
    loopTiming.markLoopEnd();
  }
}
