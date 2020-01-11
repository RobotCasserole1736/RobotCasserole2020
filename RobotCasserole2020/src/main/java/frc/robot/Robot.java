/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import frc.lib.Calibration.CalWrangler;
import frc.lib.WebServer.CasseroleDriverView;
import frc.lib.DataServer.CasseroleDataServer;
import frc.lib.Util.CasseroleCrashHandler;
import frc.lib.Util.CrashTracker;
import frc.lib.WebServer.CasseroleWebServer;
import frc.robot.Drivetrain.Drivetrain;
import frc.robot.HumanInterface.DriverController;
import frc.robot.HumanInterface.OperatorController;
import frc.robot.ControlPanel.CasseroleColorSensor;

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

  //Sensors and Cameras and stuff, oh my!
  JeVoisInterface jevois;
  CasseroleColorSensor colorSensor;

  //Shooter
  ShooterControl shooterCtrl;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    
    CrashTracker.logRobotInit();

    /* Init website utilties */
    webserver = new CasseroleWebServer();
    wrangler = new CalWrangler();
    dataServer = CasseroleDataServer.getInstance();
    jevois = JeVoisInterface.getInstance();

    OperatorController.getInstance();
    DriverController.getInstance();
    colorSensor = CasseroleColorSensor.getInstance();

    shooterCtrl = ShooterControl.getInstance();

    Drivetrain.getInstance();

    loopTiming = LoopTiming.getInstance();

    /* Website Setup */
    initDriverView();

    dataServer.startServer();
    webserver.startServer();
    
  }
  
    
  

  @Override
  public void disabledInit() {
    CrashTracker.logDisabledInit();
    dataServer.logger.stopLogging();
  }

  @Override
  public void disabledPeriodic() {
    loopTiming.markLoopStart();
    CrashTracker.logDisabledPeriodic();
    Drivetrain.getInstance().update();
    colorSensor.update();
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    CrashTracker.logAutoInit();
    dataServer.logger.startLoggingAuto();
  }

  @Override
  public void autonomousPeriodic() {
    loopTiming.markLoopStart();
    CrashTracker.logAutoPeriodic();

    colorSensor.update();

    shooterCtrl.update();

    Drivetrain.getInstance().update();
    updateDriverView();

    loopTiming.markLoopEnd();
  }



  @Override
  public void teleopInit() {
    CrashTracker.logTeleopInit();
    dataServer.logger.startLoggingTeleop();
  }

  @Override
  public void teleopPeriodic() {
    loopTiming.markLoopStart();
    CrashTracker.logTeleopPeriodic();
    Drivetrain.getInstance().update();
    updateDriverView();
    colorSensor.update();

    shooterCtrl.update();

    loopTiming.markLoopEnd();
  }



  private void initDriverView(){
    CasseroleDriverView.newBoolean("Vision Camera Offline", "red");
    CasseroleDriverView.newBoolean("highgroundacquired", "green");
    CasseroleDriverView.newWebcam("cam1", "http://10.17.36.10:1181/stream.mjpg", 50, 75);
    CasseroleDriverView.newWebcam("cam2", "http://10.17.36.10:1182/stream.mjpg", 50, 75);

  }

  public void updateDriverView(){
    CasseroleDriverView.setBoolean("Vision Camera Offline", !jevois.isVisionOnline());
    CasseroleDriverView.setBoolean("highgroundacquired",OperatorController.getInstance().createSound());
  }

  @Override
  public void testInit() {
    
  }

  @Override
  public void testPeriodic() {
    loopTiming.markLoopStart();

    loopTiming.markLoopEnd();
  }
}
