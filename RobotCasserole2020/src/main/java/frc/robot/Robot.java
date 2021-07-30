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
import frc.lib.Calibration.CalWrangler;
import frc.lib.Calibration.Calibration;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;
import frc.lib.Signal.SignalWrangler;
import frc.lib.Util.CrashTracker;
import frc.lib.Webserver2.Webserver2;
import frc.lib.miniNT4.NT4Server;
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
  Webserver2 webserver;
  LoopTiming loopTiming;
  PowerDistributionPanel pdp;
  CasseroleRIOLoadMonitor loadMon;
  Dashboard db;

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

    NT4Server.getInstance(); // Ensure it starts

    /* Init website utilties */
    webserver = new Webserver2();
    CalWrangler.getInstance();
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

    db = new Dashboard(webserver);

    SignalWrangler.getInstance().registerSignals(this);

    webserver.startServer();

  }

  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~ DISABLED MODE
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  @Override
  public void disabledInit() {
    CrashTracker.logDisabledInit();
    SignalWrangler.getInstance().logger.stopLogging();
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

    db.updateDriverView();
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
    SignalWrangler.getInstance().logger.startLoggingAuto();
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

    db.updateDriverView();

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
    SignalWrangler.getInstance().logger.startLoggingTeleop();
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

      if (DriverController.getInstance().getSnailModeDesired()) {
        // fine movement mode
        double turnVal = DriverController.getInstance().getRotateCmd();
        double speedVal = DriverController.getInstance().getFwdRevCmd();
        turnVal *= 0.55;
        speedVal *= 0.5;
        drivetrain.setOpenLoopCmd(speedVal, turnVal);
      } else {
        // Open loop control of motors
        drivetrain.setOpenLoopCmd(DriverController.getInstance().getFwdRevCmd(),
            DriverController.getInstance().getRotateCmd());
      }
    }

    drivetrain.update();
    supperstructure.update();

    climber.update();

    db.updateDriverView();

    // put all teleop periodic code before this
    loopTiming.markLoopEnd();

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
