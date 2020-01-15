/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
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
import frc.lib.WebServer.CasseroleWebServer;
import frc.lib.DataServer.Signal;
import frc.robot.Drivetrain.Drivetrain;
import frc.robot.HumanInterface.DriverController;
import frc.robot.HumanInterface.OperatorController;
import edu.wpi.first.wpilibj.RobotController;
import frc.lib.LoadMon.CasseroleRIOLoadMonitor;

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

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    

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

    Drivetrain.getInstance();

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
    
  

  @Override
  public void disabledInit() {
    dataServer.logger.stopLogging();
  }

  @Override
  public void disabledPeriodic() {
    telemetryUpdate();
    Drivetrain.getInstance().update();
  }

  @Override
  public void autonomousInit() {
    dataServer.logger.startLoggingAuto();
  }

  @Override
  public void autonomousPeriodic() {
    Drivetrain.getInstance().update();
    updateDriverView();
    loopTiming.markLoopStart();
    telemetryUpdate();



    // put all code before this 
    loopTiming.markLoopEnd();
    
    
  }



  @Override
  public void teleopInit() {
    dataServer.logger.startLoggingTeleop();
  }

  @Override
  public void teleopPeriodic() {
    Drivetrain.getInstance().update();
    updateDriverView();
    loopTiming.markLoopStart();
    telemetryUpdate();

    // put all code before this 
    loopTiming.markLoopEnd();
    
  }



  private void initDriverView(){
    CasseroleDriverView.newBoolean("Vision Camera Offline", "red");
    CasseroleDriverView.newBoolean("High Ground Aquired", "green");
    CasseroleDriverView.newWebcam("cam1", "http://10.17.36.10:1181/stream.mjpg", 50, 75);
    CasseroleDriverView.newWebcam("cam2", "http://10.17.36.10:1182/stream.mjpg", 50, 75);

  }

  public void updateDriverView(){
    CasseroleDriverView.setBoolean("Vision Camera Offline", !jevois.isVisionOnline());
    CasseroleDriverView.setBoolean("High Ground Aquired" , true);
  }

  @Override
  public void testInit() {
    
  }

  @Override
  public void testPeriodic() {
    telemetryUpdate();

  }
}
