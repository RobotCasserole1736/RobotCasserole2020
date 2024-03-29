/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2020 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.revrobotics.CANSparkMax.IdleMode;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import frc.lib.Calibration.CalWrangler;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.CasseroleDataServer;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;
import frc.lib.Util.CrashTracker;
import frc.lib.WebServer.CasseroleDriverView;
import frc.lib.WebServer.CasseroleWebServer;
import frc.robot.Autonomous.Autonomous;
import frc.robot.BallHandling.Conveyor;
import frc.robot.BallHandling.Hopper;
import frc.robot.BallHandling.IntakeControl;
import frc.robot.BallHandling.Conveyor.ConveyorOpMode;
import frc.robot.ControlPanel.ControlPanelStateMachine;
import frc.robot.Drivetrain.Drivetrain;
import frc.robot.HumanInterface.DriverController;
import frc.robot.HumanInterface.OperatorController;
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

  // Website utilities
  CasseroleWebServer webserver;
  CalWrangler wrangler;
  CasseroleDataServer dataServer;
  LoopTiming loopTiming;
  PowerDistributionPanel pdp;
  CasseroleRIOLoadMonitor loadMon;

  // Autonomous Control Utilities
  Autonomous auto;

  // Sensors and Cameras and stuff, oh my!
  CasseroleVision cam;

  VisionLEDRingControl eyeOfVeganSauron;

  // Subsystems
  Drivetrain drivetrain;
  ShooterControl shooterCtrl;
  IntakeControl intakeCtrl;
  Hopper hopper;
  Climber climber;
  PneumaticsControl thbbtbbtbbtbbt;
  MiscTelemetry telemetry;
  LEDController ledCont;

  Supperstructure supperstructure; // A misspelling you say? Ha! Wrong you are! Imagery is even baked into our
                                   // source code.

  // Misc.
  Calibration snailModeLimitRPM;
  boolean climberUpperLSPressed;
  boolean climberLowerLSPressed;
  boolean conveyorFull;
  boolean pneumaticPressureLow;

  int slowLoopCounter = 0;
  final int SLOW_LOOP_RATE = 10; // 200ms loop

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~ Robot Init
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  @Override
  public void robotInit() {

    CrashTracker.logRobotInit();

    /* Init website utilties */
    webserver = new CasseroleWebServer();
    wrangler = new CalWrangler();
    dataServer = CasseroleDataServer.getInstance();
    cam = CasseroleVision.getInstance();
    pdp = CasserolePDP.getInstance();
    loadMon = new CasseroleRIOLoadMonitor();

    thbbtbbtbbtbbt = PneumaticsControl.getInstance();
    eyeOfVeganSauron = VisionLEDRingControl.getInstance();

    OperatorController.getInstance();
    DriverController.getInstance();

    shooterCtrl = ShooterControl.getInstance();

    drivetrain = Drivetrain.getInstance();

    auto = Autonomous.getInstance();

    intakeCtrl = IntakeControl.getInstance();

    hopper = Hopper.getInstance();

    climber = Climber.getInstance();

    loopTiming = LoopTiming.getInstance();

    supperstructure = Supperstructure.getInstance();

    ControlPanelStateMachine.getInstance();

    telemetry = MiscTelemetry.getInstance();

    snailModeLimitRPM = new Calibration("Snail Mode Max Wheel Speed (RPM)", 200, 0, 1000);

    ledCont = LEDController.getInstance();

    /* Website Setup */
    initDriverView();

    dataServer.registerSignals(this);
    dataServer.startServer();
    webserver.startServer();

    LiveWindow.disableAllTelemetry();

  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~ DISABLED MODE
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  @Override
  public void disabledInit() {
    CrashTracker.logDisabledInit();
    dataServer.logger.stopLogging();
    drivetrain.setMotorMode(IdleMode.kCoast);
    auto.reset();

    telemetry.timeTracker.logAndReset();
    thbbtbbtbbtbbt.timeTracker.logAndReset();
    shooterCtrl.timeTracker.logAndReset();

  }

  @Override
  public void disabledPeriodic() {

    loopTiming.markLoopStart();
    CrashTracker.logDisabledPeriodic();

    slowLoopCounter++;
    if (slowLoopCounter % SLOW_LOOP_RATE == 0) {
      eyeOfVeganSauron.setLEDRingState(false);
      auto.sampleDashboardSelector();

      drivetrain.updateGains(false);
      shooterCtrl.updateGains(false);
      climber.update();

      if (RobotController.getUserButton() == true) {
        drivetrain.calGyro();
      }

    }

    if (OperatorController.getInstance().getshotSpeedIncrementCmd()) {
      shooterCtrl.incrementSpeedSetpoint();
    } else if (OperatorController.getInstance().getshotSpeedDecrementCmd()) {
      shooterCtrl.decrementSpeedSetpoint();
    }

    cam.update();

    supperstructure.setClearJamDesired(false);
    supperstructure.setEjectDesired(false);
    supperstructure.setEstopDesired(false);
    supperstructure.setIntakeDesired(false);
    supperstructure.setPrepToShootDesired(false);
    supperstructure.setShootFarDesired(false);
    supperstructure.setShootCloseDesired(false);
    supperstructure.update();

    drivetrain.setOpenLoopCmd(0, 0);
    drivetrain.update();

    updateDriverView();
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
    drivetrain.setMotorMode(IdleMode.kCoast);
    auto.sampleDashboardSelector();
    auto.startSequencer(); // Actually trigger the start of whatever autonomous routine we're doing
    eyeOfVeganSauron.setLEDRingState(true);
    cam.setInnerGoalAsTarget(false); // just 2's for auto

  }

  @Override
  public void autonomousPeriodic() {

    loopTiming.markLoopStart();
    CrashTracker.logAutoPeriodic();
    // Put all auto periodic code after this

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
    drivetrain.setMotorMode(IdleMode.kBrake);
    eyeOfVeganSauron.setLEDRingState(true);
    cam.setInnerGoalAsTarget(true); // try for 3's in teleop
  }

  @Override
  public void teleopPeriodic() {
    loopTiming.markLoopStart();
    CrashTracker.logTeleopPeriodic();
    // Put all teleop periodic code after this

    DriverController.getInstance().update();
    OperatorController.getInstance().update();

    cam.update();

    if (OperatorController.getInstance().getshotSpeedIncrementCmd()) {
      shooterCtrl.incrementSpeedSetpoint();
    } else if (OperatorController.getInstance().getshotSpeedDecrementCmd()) {
      shooterCtrl.decrementSpeedSetpoint();
    }

    supperstructure.setClearJamDesired(OperatorController.getInstance().getUnjamCmd());
    supperstructure.setEjectDesired(OperatorController.getInstance().getEjectDesired());
    supperstructure.setEstopDesired(false); // TODO
    supperstructure.setIntakeDesired(OperatorController.getInstance().getIntakeDesired());
    supperstructure.setPrepToShootDesired(OperatorController.getInstance().getPrepToShootCmd());

    auto.sampleOperatorCommands();
    auto.update();

    if (auto.isActive()) {
      // Nothing to do. Expect that auto sequencer will provide drivetrain & some
      // superstructure
    } else {
      // Driver & operator control in manual
      supperstructure.setShootFarDesired(OperatorController.getInstance().getShootCmd());

      double turnVal = DriverController.getInstance().getRotateCmd();
      double speedVal = DriverController.getInstance().getFwdRevCmd();

      if (DriverController.getInstance().getSnailModeDesired()) {
        // fine movement mode
        speedVal *= 0.5;
      } 

      drivetrain.setOpenLoopCmd(speedVal, turnVal);

    }

    drivetrain.update();
    supperstructure.update();

    climber.update();

    ControlPanelStateMachine.getInstance().update();

    updateDriverView();

    // put all teleop periodic code before this
    loopTiming.markLoopEnd();

  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~ UTILITIES
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  private void initDriverView() {
    CasseroleDriverView.newDial("System Press (PSI)", 0, 150, 10, 90, 130);
    CasseroleDriverView.newDial("Shooter Speed (RPM)", 0, 6000, 600, 4500, 5700);
    CasseroleDriverView.newDial("Robot Speed (fps)", 0, 20, 2, 5, 15);
    CasseroleDriverView.newDial("Vision Tgt Angle (deg)", -30, 30, 5, -2.5, 2.5);
    CasseroleDriverView.newBoolean("Master Caution", "red");
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
    CasseroleDriverView.newStringBox("Shooter Setpoint");
    CasseroleDriverView.newSoundWidget("High Ground Acqd", "./highground.mp3");
    CasseroleDriverView.newAutoSelector("Action", Autonomous.ACTION_MODES);
    CasseroleDriverView.newAutoSelector("Delay", Autonomous.DELAY_OPTIONS);
    CasseroleDriverView.newWebcam("cam1", "http://10.17.36.10:1181/stream.mjpg", 50, 75);
    CasseroleDriverView.newWebcam("cam2", "http://10.17.36.10:1182/stream.mjpg", 50, 75);

  }

  int masterCautionBlinkCounter = 0;
  final int MASTER_CAUTION_BLINK_LOOPS = 15;
  boolean masterCautionIndicatorState = false;

  public void updateDriverView() {
    if (Conveyor.getInstance().getUpperSensorValue() == true && Conveyor.getInstance().getOpMode() == ConveyorOpMode.AdvanceFromHopper) {
      conveyorFull = true;
    } else {
      conveyorFull = false;
    }

    if (PneumaticsControl.getInstance().getPressure() < 60) {
      pneumaticPressureLow = true;
    } else {
      pneumaticPressureLow = false;
    }

    climberUpperLSPressed = (climber.upperLSVal == TwoWireParitySwitch.SwitchState.Pressed);
    climberLowerLSPressed = (climber.lowerLSVal == TwoWireParitySwitch.SwitchState.Pressed);

    CasseroleDriverView.setDialValue("System Press (PSI)", thbbtbbtbbtbbt.getPressure());
    CasseroleDriverView.setDialValue("Shooter Speed (RPM)", shooterCtrl.getSpeedRPM());
    CasseroleDriverView.setDialValue("Robot Speed (fps)", drivetrain.getRobotSpeedfps());
    CasseroleDriverView.setDialValue("Vision Tgt Angle (deg)", cam.isTgtVisible() ? -1.0 * cam.getTgtGeneralAngle() : -50);
    CasseroleDriverView.setBoolean("Vision Camera Fault", !cam.isVisionOnline());
    CasseroleDriverView.setBoolean("Vision Target Visible", cam.isTgtVisible());
    CasseroleDriverView.setBoolean("Climber Lower SW Fault", climber.isLowerLimitSwitchFaulted());
    CasseroleDriverView.setBoolean("Climber Upper SW Fault", climber.isUpperLimitSwitchFaulted());
    CasseroleDriverView.setBoolean("Climber Upper SW Pressed", climber.isUpperLimitSwitchPressed());
    CasseroleDriverView.setBoolean("Climber Lower SW Pressed", climber.isLowerLimitSwitchPressed());
    CasseroleDriverView.setBoolean("Pnuematic Pressure", pneumaticPressureLow);
    CasseroleDriverView.setBoolean("Conveyor Full", conveyorFull);
    CasseroleDriverView.setBoolean("Shooter Spoolup", (shooterCtrl.getShooterCtrlMode() == ShooterCtrlMode.Accelerate || shooterCtrl.getShooterCtrlMode() == ShooterCtrlMode.Stabilize));
    CasseroleDriverView.setStringBox("Shots Taken", Integer.toString(shooterCtrl.getShotCount()));
    CasseroleDriverView.setStringBox("Shooter Setpoint", String.format("%.0fRPM", shooterCtrl.getAdjustedSetpointRPM()));

    if (pneumaticPressureLow || climber.isUpperLimitSwitchFaulted() || climber.isLowerLimitSwitchFaulted()) {
      masterCautionBlinkCounter++;
      if (masterCautionBlinkCounter > MASTER_CAUTION_BLINK_LOOPS) {
        masterCautionBlinkCounter = 0;
        masterCautionIndicatorState = !masterCautionIndicatorState;
      }
    } else {
      masterCautionBlinkCounter = 0;
      masterCautionIndicatorState = false;
    }

    CasseroleDriverView.setBoolean("Master Caution", masterCautionIndicatorState);

    if (DriverStation.getInstance().getMatchTime() <= 30 && Climber.getInstance().climbEnabled == true) {
      CasseroleDriverView.setSoundWidget("High Ground Acqd", true);
    } else {
      CasseroleDriverView.setSoundWidget("High Ground Acqd", false);
    }

    dataServer.sampleAllSignals();
  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~ TEST MODE
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  @Override
  public void testInit() {

  }

  @Override
  public void testPeriodic() {
    loopTiming.markLoopStart();

    loopTiming.markLoopEnd();
  }
}
