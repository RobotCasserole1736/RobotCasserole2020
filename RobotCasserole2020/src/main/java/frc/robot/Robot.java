/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2020 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.sql.Driver;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import frc.lib.Calibration.CalWrangler;
import frc.lib.WebServer.CasseroleDriverView;
import frc.lib.DataServer.CasseroleDataServer;
import frc.lib.Util.CrashTracker;
import frc.lib.WebServer.CasseroleWebServer;
import frc.lib.DataServer.Signal;
import frc.robot.Drivetrain.CasseroleGyro;
import frc.robot.Drivetrain.Drivetrain;
import frc.robot.HumanInterface.DriverController;
import frc.robot.HumanInterface.OperatorController;
import frc.robot.ShooterControl.ShooterControl;
import frc.robot.Autonomous.Autonomous;
import frc.robot.ControlPanel.CasseroleColorSensor;
import edu.wpi.first.wpilibj.RobotController;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;
import edu.wpi.first.wpilibj.DriverStation;

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

  //Sensors and Cameras and stuff, oh my!
  JeVoisInterface jevois;
  CasseroleColorSensor colorSensor;

  //Shooter
  ShooterControl shooterCtrl;

  //
  Drivetrain drivetrain;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  //Temp
  CasseroleGyro gyro_test;

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
    jevois = JeVoisInterface.getInstance();
    pdp = new PowerDistributionPanel(RobotConstants.POWER_DISTRIBUTION_PANEL_CANID);
    loadMon= new CasseroleRIOLoadMonitor();

    /* Init local telemetry signals */
    rioDSSampLoadSig = new Signal("Dataserver Stored Samples", "count"); 
    rioCurrDrawLoadSig = new Signal("Battery Current Draw", "A");
    rioBattVoltLoadSig = new Signal("Battery Voltage", "V");
    rioDSLogQueueLenSig = new Signal("Dataserver File Logger Queue Length", "count");
    rioIsBrownoutSig = new Signal("Robot Brownout", "bool");
    rioCANBusUsagePctSig = new Signal("Robot CAN Bus Utilization", "pct");

    OperatorController.getInstance();
    DriverController.getInstance();
    colorSensor = CasseroleColorSensor.getInstance();

    shooterCtrl = ShooterControl.getInstance();

    gyro_test = new CasseroleGyro();

    drivetrain = Drivetrain.getInstance();

    Autonomous.getInstance();

    loopTiming = LoopTiming.getInstance();

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
    Autonomous.getInstance().reset();

  }

  @Override
  public void disabledPeriodic() {
    loopTiming.markLoopStart();
    CrashTracker.logDisabledPeriodic();

    Autonomous.getInstance().sampleDashboardSelector();

    colorSensor.update();

    shooterCtrl.update();

    Drivetrain.getInstance().update();

    updateDriverView();
    telemetryUpdate();
    loopTiming.markLoopEnd();
  }



//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~ AUTONOMOUS MODE
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  @Override
  public void autonomousInit() {
    CrashTracker.logAutoInit();
    dataServer.logger.startLoggingAuto();
    Autonomous.getInstance().sampleDashboardSelector();
    Autonomous.getInstance().startSequencer(); //Actually trigger the start of whatever autonomous routine we're doing
  }

  @Override
  public void autonomousPeriodic() {
    loopTiming.markLoopStart();
    CrashTracker.logAutoPeriodic();

    drivetrain.update();
    Autonomous.getInstance().update();

    colorSensor.update();

    shooterCtrl.update();

    updateDriverView();
    telemetryUpdate();

    // put all code before this 
    loopTiming.markLoopEnd();
  }


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~ TELEOP MODE
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  @Override
  public void teleopInit() {
    CrashTracker.logTeleopInit();
    dataServer.logger.startLoggingTeleop();
  }

  @Override
  public void teleopPeriodic() {
    loopTiming.markLoopStart();
    CrashTracker.logTeleopPeriodic();
    drivetrain.update();
    
    colorSensor.update();

    Autonomous.getInstance().sampleOperatorCommands();
    Autonomous.getInstance().update();

    shooterCtrl.update();

    if(Autonomous.getInstance().isActive()){
      //Nothing to do, expect that auto sequencer will provide drivetrain comands
    } else {
      //Driver control in manual
      drivetrain.setOpenLoopCmd(DriverController.getInstance().getFwdRevCmd(), 
                                              DriverController.getInstance().getRotateCmd());
    }



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
    CasseroleDriverView.newBoolean("Vision Camera Offline", "red");
    CasseroleDriverView.newSoundWidget("High Ground Acqd", "./highground.mp3");
    CasseroleDriverView.newAutoSelector("Action", Autonomous.ACTION_MODES);
		CasseroleDriverView.newAutoSelector("Delay", Autonomous.DELAY_OPTIONS);
    CasseroleDriverView.newWebcam("cam1", "http://10.17.36.10:1181/stream.mjpg", 50, 75);
    CasseroleDriverView.newWebcam("cam2", "http://10.17.36.10:1182/stream.mjpg", 50, 75);

  }

  public void updateDriverView(){
    CasseroleDriverView.setBoolean("Vision Camera Offline", !jevois.isVisionOnline());
    CasseroleDriverView.setSoundWidget("High Ground Acqd",DriverStation.getInstance().isFMSAttached());
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
