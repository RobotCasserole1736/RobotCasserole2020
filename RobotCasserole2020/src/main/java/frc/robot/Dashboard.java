package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import frc.lib.Signal.SignalUtils;
import frc.lib.Signal.Annotations.Signal;
import frc.lib.Webserver2.Webserver2;
import frc.lib.Webserver2.DashboardConfig.DashboardConfig;
import frc.robot.Autonomous.Autonomous;
import frc.robot.BallHandling.Conveyor;
import frc.robot.BallHandling.Conveyor.ConveyorOpMode;
import frc.robot.ShooterControl.ShooterControl;
import frc.robot.ShooterControl.ShooterControl.ShooterCtrlMode;
import frc.robot.VisionProc.CasseroleVision;

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

        final double RIGHT_COL = 17;
        final double CENTER_COL = 50;
        final double LEFT_COL = 83;

        final double ROW1 = 15;
        final double ROW2 = 60;
        final double ROW3 = 75;
        final double ROW4 = 85;

        d.addCircularGauge(SignalUtils.nameToNT4ValueTopic("db_systemPressure"), "System Press", "PSI", 0.0, 150.0, 90.0, 130, LEFT_COL, ROW1, 1.0);
        d.addLineGauge(SignalUtils.nameToNT4ValueTopic("db_visionTargetAngle"), "Vision Tgt Angle", "deg", -30, 30, -2.5, 2.5, CENTER_COL, ROW1, 1.0);
        d.addCircularGauge(SignalUtils.nameToNT4ValueTopic("db_shooterSpeed"), "Shooter Speed", "RPM", 0, 6000, 4500, 5700, RIGHT_COL, ROW1, 1.0);

        d.addText(SignalUtils.nameToNT4ValueTopic("db_shotsTaken"), "Shots Taken", CENTER_COL, ROW1+10, 1.0);
        d.addText(SignalUtils.nameToNT4ValueTopic("db_shooterSetpoint"), "Shooter Setpoint", CENTER_COL, ROW1+20, 1.0);

        d.addCamera("cam1", "http://10.17.36.10:1181/stream.mjpg", LEFT_COL, ROW2, 0.75);
        d.addCamera("cam2", "http://10.17.36.10:1182/stream.mjpg", RIGHT_COL, ROW2, 0.75);

        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_masterCaution"),"Master Caution", "#FF0000", "icons/alert.svg", CENTER_COL-6, ROW2, 1.0);
        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_visionTargetVisible"),"Vision Target Visible", "#00FF00", "icons/vision.svg", CENTER_COL, ROW2, 1.0);
        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_climberUpperLimit"),"Climber Upper Limit", "#FFFF00", "icons/upperLimit.svg", CENTER_COL+6, ROW2, 1.0);

        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_shooterSpoolup"),"Shooter Spoolup", "#FFFF00", "icons/speed.svg", CENTER_COL-6, ROW2+5, 1.0);
        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_conveyorFull"),"Conveyor Full", "#00FF00", "icons/gear.svg", CENTER_COL, ROW2+5, 1.0);
        d.addIcon(SignalUtils.nameToNT4ValueTopic("db_climberLowerLimit"),"Climber Lower Limit", "#FFFF00", "icons/lowerLimit.svg", CENTER_COL+6, ROW2+5, 1.0);
        
        d.addSound(SignalUtils.nameToNT4ValueTopic("db_highGroundAcq"), "High Ground", "sfx/highground.mp3", false);

        d.addAutoChooser(Autonomous.getInstance().delayModeList, CENTER_COL, ROW3, 1.0);
        d.addAutoChooser(Autonomous.getInstance().mainModeList, CENTER_COL, ROW4, 1.0);
    
      }
    
      public void updateDriverView() {
        Climber climber = Climber.getInstance();

        boolean pneumaticPressureLow;


        if (Conveyor.getInstance().getUpperSensorValue() == true && Conveyor.getInstance().getOpMode() == ConveyorOpMode.AdvanceFromHopper) {
          conveyorFull = true;
        } else {
          conveyorFull = false;
        }
    
        systemPressure = PneumaticsControl.getInstance().getPressure();
        pneumaticPressureLow = (systemPressure < 60.0);

        climberUpperLimit = (climber.upperLSVal == TwoWireParitySwitch.SwitchState.Pressed);
        climberLowerLimit = (climber.lowerLSVal == TwoWireParitySwitch.SwitchState.Pressed);
    
        ShooterCtrlMode curMode = ShooterControl.getInstance().getShooterCtrlMode();
        shooterSpoolup = ( curMode == ShooterCtrlMode.Stabilize || curMode == ShooterCtrlMode.Accelerate );

        visionTargetAngle = CasseroleVision.getInstance().getTgtAngle();
        visionTargetVisible = CasseroleVision.getInstance().isTgtVisible();

        //master caution handling
        if (pneumaticPressureLow ) {
          masterCautionTxt = "Low Pneumatic Pressure";
          masterCaution = true;
        } else if( climber.isUpperLimitSwitchFaulted() ) {
          masterCautionTxt = "Climber Upper Limit Switch Fault";
          masterCaution = true;
        } else if( climber.isLowerLimitSwitchFaulted() ) {
          masterCautionTxt = "Climber Lower Limit Switch Fault";
          masterCaution = true;
        }else if( !CasseroleVision.getInstance().isVisionOnline() ) {
          masterCautionTxt = "Vision Camera Disconnected";
          masterCaution = true;
        } else {
          masterCautionTxt = "";
          masterCaution = false;
        }

        highGroundAcq = (DriverStation.getInstance().getMatchTime() <= 30 && Climber.getInstance().climbEnabled);

      }
    

}
