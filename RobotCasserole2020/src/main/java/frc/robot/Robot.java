/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2020 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.lib.Calibration.CalWrangler;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.CasseroleDataServer;
import frc.lib.DataServer.Signal;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;
import frc.lib.Util.CrashTracker;
import frc.lib.WebServer.CasseroleDriverView;
import frc.lib.WebServer.CasseroleWebServer;
import frc.robot.LEDController.LEDPatterns;
import frc.robot.Autonomous.Autonomous;
import frc.robot.BallHandling.Conveyor;
import frc.robot.BallHandling.Hopper;
import frc.robot.BallHandling.IntakeControl;
import frc.robot.BallHandling.Conveyor.ConveyorOpMode;
import frc.robot.ControlPanel.ControlPanelColor;
import frc.robot.ControlPanel.ControlPanelManipulator;
import frc.robot.ControlPanel.ControlPanelStateMachine;
import frc.robot.Drivetrain.Drivetrain;
import frc.robot.HumanInterface.DriverController;
import frc.robot.HumanInterface.OperatorController;
import frc.robot.HumanInterface.PlayerFeedback;
import frc.robot.ShooterControl.ShooterControl;
import frc.robot.ShooterControl.ShooterControl.ShooterCtrlMode;
import frc.robot.VisionProc.CasseroleVision;

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
  Signal rioBattCurrDrawSig;
  Signal rioBattVoltSig;
  Signal rioSupplyVoltSig;
  Signal rioIsBrownoutSig;
  Signal rio6VBusVoltageSig;
  Signal rio5VBusVoltageSig;
  Signal rio3V3BusVoltageSig;
  Signal rioCANBusUsagePctSig;
  Signal pdpUpperBoardAuxCurrentSig;
  Signal pdpCoolingFansCurrentSig;

  //Autonomous Control Utilities
  Autonomous auto;

  //Sensors and Cameras and stuff, oh my!
  CasseroleVision cam;
  //PhotonCannonControl photonCannon;
  VisionLEDRingControl eyeOfVeganSauron;

  //Subsystems
  Drivetrain drivetrain;
  ShooterControl shooterCtrl;
  IntakeControl intakeCtrl;
  Hopper hopper;
  Climber climber;
  PneumaticsControl thbbtbbtbbtbbt;
  ControlPanelStateMachine ctrlPanel;
  ControlPanelManipulator ctrlPanelManipulator;
  RobotTilt robotTilt;
  
  LEDController ledController;
  Supperstructure supperstructure; //A misspelling you say? Ha! Wrong you are! Imagery is even baked into our source code.

  //Misc.
  Calibration snailModeLimitRPM;
  PlayerFeedback pfb;
  boolean climberUpperLSPressed;
  boolean climberLowerLSPressed;
  boolean conveyorFull;
  boolean pneumaticPressureLow;


  int slowLoopCounter = 0;
  final int SLOW_LOOP_RATE = 10; //200ms loop

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
    rioBattCurrDrawSig = new Signal("Robot Battery Current Draw", "A");
    rioBattVoltSig = new Signal("Robot Battery Voltage", "V");
    rioSupplyVoltSig = new Signal("RIO Input Voltage", "V");
    rio3V3BusVoltageSig = new Signal("RIO 3V Supply Voltage", "V");
    rio5VBusVoltageSig = new Signal("RIO 5V Supply Voltage", "V");
    rio6VBusVoltageSig = new Signal("RIO 6V Supply Voltage", "V");
    rioDSLogQueueLenSig = new Signal("Dataserver File Logger Queue Length", "count");
    rioIsBrownoutSig = new Signal("Robot Brownout", "bool");
    rioCANBusUsagePctSig = new Signal("Robot CAN Bus Utilization", "pct");
    pdpUpperBoardAuxCurrentSig = new Signal("PDP Upper Board Current", "A");
    pdpCoolingFansCurrentSig = new Signal("PDP Cooling Fans Current", "A");

    thbbtbbtbbtbbt = PneumaticsControl.getInstance();
    eyeOfVeganSauron = VisionLEDRingControl.getInstance();
    //photonCannon = PhotonCannonControl.getInstance();
    ledController = LEDController.getInstance();


    OperatorController.getInstance();
    DriverController.getInstance();
    ctrlPanel = ControlPanelStateMachine.getInstance();

    ctrlPanelManipulator=ControlPanelManipulator.getInstance();

    shooterCtrl = ShooterControl.getInstance();

    drivetrain = Drivetrain.getInstance();

    auto = Autonomous.getInstance();

    intakeCtrl = IntakeControl.getInstance();

    hopper = Hopper.getInstance();

    climber= Climber.getInstance();

    loopTiming = LoopTiming.getInstance();

    supperstructure = Supperstructure.getInstance();

    ControlPanelStateMachine.getInstance();

    robotTilt = RobotTilt.getInstance();

    snailModeLimitRPM = new Calibration("Snail Mode Max Wheel Speed (RPM)", 200, 0, 1000);

    pfb = PlayerFeedback.getInstance();

    /* Website Setup */
    initDriverView();

    dataServer.startServer();
    webserver.startServer();
    
  }
  
  public void telemetryUpdate(){
    double sampleTimeMs = loopTiming.getLoopStartTimeSec()*1000.0;

    climberUpperLSPressed = (climber.upperLSVal == TwoWireParitySwitch.SwitchState.Pressed);
    climberLowerLSPressed = (climber.lowerLSVal == TwoWireParitySwitch.SwitchState.Pressed);

    if(Conveyor.getInstance().getUpperSensorValue() == true && Conveyor.getInstance().getOpMode() == ConveyorOpMode.AdvanceFromHopper){
      conveyorFull = true;
    }else{
      conveyorFull = false;
    }

    if(PneumaticsControl.getInstance().getPressure() < 60){
      pneumaticPressureLow = true;
    }else{
      pneumaticPressureLow = false;
    }
    
    rioDSSampLoadSig.addSample(sampleTimeMs, dataServer.getTotalStoredSamples());
    rioBattCurrDrawSig.addSample(sampleTimeMs,  CasserolePDP.getInstance().getTotalCurrent());
    rioBattVoltSig.addSample(sampleTimeMs,  CasserolePDP.getInstance().getVoltage());  
    rioSupplyVoltSig.addSample(sampleTimeMs,  RobotController.getInputVoltage());  
    rioDSLogQueueLenSig.addSample(sampleTimeMs, dataServer.logger.getSampleQueueLength());
    rioIsBrownoutSig.addSample(sampleTimeMs, RobotController.isBrownedOut());
    rioCANBusUsagePctSig.addSample(sampleTimeMs, RobotController.getCANStatus().percentBusUtilization);
    pdpUpperBoardAuxCurrentSig.addSample(sampleTimeMs, CasserolePDP.getInstance().getCurrent(RobotConstants.UPPER_BOARD_AUX_PDP_CHANNEL));
    pdpCoolingFansCurrentSig.addSample(sampleTimeMs, CasserolePDP.getInstance().getCurrent(RobotConstants.COOLING_FANS_PDP_CHANNEL));
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

      slowLoopCounter++;
      if(slowLoopCounter%SLOW_LOOP_RATE == 0){
        ledController.setPattern(LEDPatterns.Pattern0); //Defaults to disabled. We can't actually change this
        thbbtbbtbbtbbt.update();
        eyeOfVeganSauron.setLEDRingState(false);
        auto.sampleDashboardSelector();

        ctrlPanelManipulator.updateGains(false);
        drivetrain.updateGains(false);
        shooterCtrl.updateGains(false);
        pfb.update();
        robotTilt.update();
        climber.update();

        if(RobotController.getUserButton() == true){
          drivetrain.calGyro();
        }

      }

      cam.update();

      ctrlPanel.update();
      ctrlPanelManipulator.update();

      supperstructure.setClearJamDesired(false);
      supperstructure.setEjectDesired(false);
      supperstructure.setEstopDesired(false);
      supperstructure.setIntakeDesired(false);
      supperstructure.setPrepToShootDesired(false);
      supperstructure.setShootDesired(false);
      supperstructure.update();

      drivetrain.setOpenLoopCmd(0, 0);
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
      //Put all auto periodic code after this
  
      slowLoopCounter++;
      if(slowLoopCounter%SLOW_LOOP_RATE == 0){
        thbbtbbtbbtbbt.update();
        eyeOfVeganSauron.setLEDRingState(true);
        ledUpdater();
        ctrlPanel.update();
        ctrlPanelManipulator.update();
        robotTilt.update();
        telemetryUpdate();
        pfb.update();
        cam.setInnerGoalAsTarget(false); //just 2's for auto
      }

      cam.update();

      auto.update();

      supperstructure.update();

      drivetrain.update();

      climber.update();
      
      updateDriverView();

      // put all auto periodic code before this
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
      //Put all teleop periodic code after this


      DriverController.getInstance().update();
      OperatorController.getInstance().update();

      slowLoopCounter++;
      if(slowLoopCounter%SLOW_LOOP_RATE == 0){
        thbbtbbtbbtbbt.update();
        ctrlPanel.update();
        ctrlPanelManipulator.update();
        ledController.update();
        robotTilt.update();
        pfb.update();
        ledUpdater();
        telemetryUpdate();
        eyeOfVeganSauron.setLEDRingState(true);
        cam.setInnerGoalAsTarget(true); //try for 3's in teleop
      }

      cam.update();

      supperstructure.setClearJamDesired(OperatorController.getInstance().getUnjamCmd());
      supperstructure.setEjectDesired(OperatorController.getInstance().getEjectDesired());
      supperstructure.setEstopDesired(false); //TODO
      supperstructure.setIntakeDesired(OperatorController.getInstance().getIntakeDesired());
      supperstructure.setPrepToShootDesired(OperatorController.getInstance().getPrepToShootCmd());

      auto.sampleOperatorCommands();
      auto.update();

      if(auto.isActive()){
        //Nothing to do. Expect that auto sequencer will provide drivetrain & some superstructure
      } else {
        //Driver & operator control in manual
        supperstructure.setShootDesired(OperatorController.getInstance().getShootCmd());

        if(DriverController.getInstance().getSnailModeDesired()){
          //Closed-loop, fine movement mode
          double spd = snailModeLimitRPM.get();
          double leftCmdRPM  = spd*(DriverController.getInstance().getFwdRevCmd() - DriverController.getInstance().getRotateCmd());
          double rightCmdRPM = spd*(DriverController.getInstance().getFwdRevCmd() + DriverController.getInstance().getRotateCmd());
          drivetrain.setClosedLoopSpeedCmd(leftCmdRPM, rightCmdRPM);
        } else {
          //Open loop control of motors
          drivetrain.setOpenLoopCmd(DriverController.getInstance().getFwdRevCmd(), 
                                    DriverController.getInstance().getRotateCmd());
        }
      }

      drivetrain.update();
      supperstructure.update();
  
      climber.update();
      
      updateDriverView();

      // put all teleop periodic code before this 
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
    CasseroleDriverView.newDial("Robot Angle (deg)", -180, 180, 45, -10, 10);
    CasseroleDriverView.newDial("Vision Tgt Angle (deg)", -30, 30, 5, -2.5, 2.5);
    CasseroleDriverView.newBoolean("Vision Camera Fault", "red");
    CasseroleDriverView.newBoolean("Vision Target Visible", "green");
    CasseroleDriverView.newBoolean("Climber Lower SW Fault", "red");
    CasseroleDriverView.newBoolean("Climber Upper SW Fault", "red");
    CasseroleDriverView.newBoolean("Climber Upper SW Pressed", "yellow");
    CasseroleDriverView.newBoolean("Climber Lower SW Pressed", "yellow");
    CasseroleDriverView.newBoolean("Conveyor Full", "green");
    CasseroleDriverView.newBoolean("Pnuematic Pressure", "red");
    CasseroleDriverView.newBoolean("Shooter Spoolup", "yellow");
    CasseroleDriverView.newStringBox("Shots Taken");
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
    CasseroleDriverView.setDialValue("Robot Angle (deg)", robotTilt.getRobotAngle());
    CasseroleDriverView.setDialValue("Vision Tgt Angle (deg)", cam.isTgtVisible()?-1.0*cam.getTgtGeneralAngle():-50);
    CasseroleDriverView.setBoolean("Vision Camera Fault", !cam.isVisionOnline());
    CasseroleDriverView.setBoolean("Vision Target Visible", cam.isTgtVisible());
    CasseroleDriverView.setBoolean("Climber Lower SW Fault", climber.isLowerLimitSwitchFaulted());
    CasseroleDriverView.setBoolean("Climber Upper SW Fault", climber.isUpperLimitSwitchFaulted());
    CasseroleDriverView.setBoolean("Climber Upper SW Pressed", climber.isUpperLimitSwitchPressed());
    CasseroleDriverView.setBoolean("Climber Lower SW Pressed", climber.isLowerLimitSwitchPressed());
    CasseroleDriverView.setBoolean("Pnuematic Pressure", pneumaticPressureLow);
    CasseroleDriverView.setBoolean("Conveyor Full", conveyorFull);
    CasseroleDriverView.setBoolean("Shooter Spoolup", (shooterCtrl.getShooterCtrlMode() == ShooterCtrlMode.SpoolUp));
    CasseroleDriverView.setStringBox("Shots Taken", Integer.toString(shooterCtrl.getShotCount()));
    
    if (DriverStation.getInstance().getMatchTime() <= 30 && Climber.getInstance().climbEnabled == true){
      CasseroleDriverView.setSoundWidget("High Ground Acqd",true);
    }else{
      CasseroleDriverView.setSoundWidget("High Ground Acqd",false); 
    }
  }

  public void ledUpdater(){
    if (DriverStation.getInstance().getMatchTime() <= 30 && Climber.getInstance().climbEnabled == true){
      ledController.setPattern(LEDPatterns.Pattern6);
    }
    else if(ctrlPanelManipulator.isRotationCompleted() == false){
      ledController.setPattern(LEDPatterns.Pattern6);
    }
    else if(ControlPanelStateMachine.getInstance().getGameDataColor() == ControlPanelColor.kRED){
      ledController.setPattern(LEDPatterns.Pattern0);
    }
    else if(ControlPanelStateMachine.getInstance().getGameDataColor() == ControlPanelColor.kBLUE){
      ledController.setPattern(LEDPatterns.Pattern1);
    }
    else if(ControlPanelStateMachine.getInstance().getGameDataColor() == ControlPanelColor.kYELLOW){
      ledController.setPattern(LEDPatterns.Pattern3);
    }
    else if(ControlPanelStateMachine.getInstance().getGameDataColor() == ControlPanelColor.kGREEN){
      ledController.setPattern(LEDPatterns.Pattern2);
    }
    else if(DriverStation.getInstance().getAlliance() == DriverStation.Alliance.Blue){
      if(DriverStation.getInstance().isAutonomous() == true){
        ledController.setPattern(LEDPatterns.Pattern1);
      }
      else{
        ledController.setPattern(LEDPatterns.Pattern4);
      }
    }
    else if(DriverStation.getInstance().getAlliance() == DriverStation.Alliance.Red){
      if(DriverStation.getInstance().isAutonomous() == true){
        ledController.setPattern(LEDPatterns.Pattern0);
      }
      else{
        ledController.setPattern(LEDPatterns.Pattern5);
      }
    }
    ledController.update();
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
