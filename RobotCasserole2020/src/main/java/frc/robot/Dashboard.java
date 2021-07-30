package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import frc.lib.Signal.SignalUtils;
import frc.lib.Signal.SignalWrangler;
import frc.lib.Signal.Annotations.Signal;
import frc.lib.Webserver2.Webserver2;
import frc.lib.Webserver2.DashboardConfig.DashboardConfig;
import frc.robot.BallHandling.Conveyor;
import frc.robot.BallHandling.Conveyor.ConveyorOpMode;

public class Dashboard {

    @Signal(name = "db_systemPressure")
    double systemPressure;

    @Signal(name = "db_shooterSpeed")
    double shooterSpeed;

    @Signal(name = "db_visionTargetAngle")
    double visionTargetAngle;


    @Signal(name = "db_visionTargetVisible")
    boolean visionTargetVisible;

    @Signal(name = "db_climberUpperLimit")
    boolean climberUpperLimit;
    
    @Signal(name = "db_climberLowerLimit")
    boolean climberLowerLimit;

    @Signal(name = "db_conveyorFull")
    boolean conveyorFull;

    @Signal(name = "db_shooterSpoolup")
    boolean shooterSpoolup;

    @Signal(name = "db_shooterSetpoint")
    double shooterSetpoint;

    @Signal(name = "db_shotsTaken")
    double shotsTaken;

    @Signal(name = "db_highGroundAcq")
    boolean highGroundAcq;



    @Signal(name = "db_masterCaution")
    boolean masterCaution;

    String masterCautionTxt;

    DashboardConfig d;

    public Dashboard (Webserver2 ws_in) {
        d = ws_in.dashboard;

        d.addCircularGauge(SignalUtils.nameToNT4ValueTopic("db_systemPressure"), "System Press", "PSI", 0.0, 150.0, 90.0, 130, 5.0, 5.0, 1.0);

        d.addCircularGauge(SignalUtils.nameToNT4ValueTopic("db_shooterSpeed"), "Shooter Speed", "RPM", 0, 6000, 4500, 5700, 5.0, 5.0, 1.0);
        d.addText(SignalUtils.nameToNT4ValueTopic("db_shooterSetpoint"), "Shooter Setpoint", 5, 82.5, 1.0);

        d.addLineGauge(SignalUtils.nameToNT4ValueTopic("db_visionTargetAngle"), "Vision Tgt Angle", "deg", -30, 30, -2.5, 2.5, 5.0, 5.0, 1.0);
        
        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_masterCaution"),"Master Caution", "#FF0000", "icons/alert.svg", 30, 5, 1.0);
        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_visionTargetVisible"),"Vision Target Visible", "#00FF00", "icons/vision.svg", 30, 5, 1.0);
        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_climberUpperLimit"),"Climber Upper Limit", "#FFFF00", "icons/upperLimit.svg", 30, 5, 1.0);
        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_climberLowerLimit"),"Climber Lower Limit", "#FFFF00", "icons/lowerLimit.svg", 30, 5, 1.0);
        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_conveyorFull"),"Conveyor Full", "#00FF00", "icons/gear.svg", 30, 5, 1.0);
        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_shooterSpoolup"),"Shooter Spoolup", "#FFFF00", "icons/speed.svg", 30, 5, 1.0);
        
        d.addText(SignalUtils.nameToNT4ValueTopic("db_shotsTaken"), "Shots Taken", 5, 82.5, 1.0);

        d.addCamera("", "cam1", "http://10.17.36.10:1181/stream.mjpg", 30, 17, 1.0);
        d.addCamera("", "cam1", "http://10.17.36.10:1182/stream.mjpg", 30, 17, 1.0);

        d.addSound(SignalUtils.nameToNT4ValueTopic("db_highGroundAcq"), "High Ground", "sfx/highground.mp3", false);
    
      }
    
    
      public void updateDriverView() {
        Climber climber = Climber.getInstance();

        boolean pneumaticPressureLow;


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
    
        climberUpperLimit = (climber.upperLSVal == TwoWireParitySwitch.SwitchState.Pressed);
        climberLowerLimit = (climber.lowerLSVal == TwoWireParitySwitch.SwitchState.Pressed);
    

        //master caution handling
        if (pneumaticPressureLow || climber.isUpperLimitSwitchFaulted() || climber.isLowerLimitSwitchFaulted()) {

        }

        highGroundAcq = (DriverStation.getInstance().getMatchTime() <= 30 && Climber.getInstance().climbEnabled);

        SignalWrangler.getInstance().sampleAllSignals();
      }
    

}
