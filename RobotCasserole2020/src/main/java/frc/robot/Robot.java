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
import frc.lib.WebServer.CasseroleWebServer;
import frc.robot.Drivetrain.Drivetrain;
import frc.robot.HumanInterface.DriverController;
import frc.robot.HumanInterface.OperatorController;

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

    OperatorController.getInstance();
    DriverController.getInstance();

    Drivetrain.getInstance();

    loopTiming = LoopTiming.getInstance();

    /* Website Setup */
    initDriverView();

    
    dataServer.startServer();
    webserver.startServer();
  }
  
    
  

  @Override
  public void disabledInit() {
    dataServer.logger.stopLogging();
  }

  @Override
  public void disabledPeriodic() {
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

  }
}
