package frc.robot.ControlPanel;

import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import frc.robot.Robot;
import frc.robot.RobotConstants;

public class ControlPanelManipulator {

    /* Singelton Stuff */
    private static ControlPanelManipulator instance = null;
    public static synchronized ControlPanelManipulator getInstance() {
        if(instance == null)
        instance = new ControlPanelManipulator();
        return instance;
    }

    boolean rotationComplete = true;
    double desiredRotation_deg = 0.0;
    CANSparkMax ControlPanelMotor;

    CANPIDController controlPanelPID;
    Calibration ErrorLimit;
    Calibration kP;
    Calibration kI;
    Calibration kD;
    Calibration kFF;

    Signal ControlPanelMotorCurrentSignal;
    Signal ControlPanelDesiredAngleSignal;
    Signal ControlPanelActualAngleSignal;

    boolean calsUpdated;



// 88% speed should be good


    private ControlPanelManipulator(){
        kP = new Calibration("Control Panel P Value", 0.1);
        kI = new Calibration("Control Panel I Value", 0);
        kD = new Calibration("Control Panel D Value", 0);
        kFF = new Calibration("Control Panel F Value", 0.0);
        ErrorLimit = new Calibration("Control Panel Maximum Error",10);
        if(Robot.isReal()){
            ControlPanelMotor= new CANSparkMax(RobotConstants.CONTROL_PANEL_MANIPULATOR_CAN_ID, MotorType.kBrushless);
            ControlPanelMotor.restoreFactoryDefaults();
            ControlPanelMotor.setSmartCurrentLimit(30);
            ControlPanelMotorCurrentSignal = new Signal("Control Panel Motor Current","A");
            ControlPanelActualAngleSignal = new Signal("Control Panel Actual Angle","deg");
            controlPanelPID = new CANPIDController(ControlPanelMotor);
            ControlPanelMotor.getEncoder().setPositionConversionFactor(RobotConstants.CONTROL_PANEL_MANIPULATOR_RATIO);
            updateGains(true);
            ControlPanelMotor.setClosedLoopRampRate(0);
            ControlPanelMotor.burnFlash();
            desiredRotation_deg = ControlPanelMotor.getEncoder().getPosition();
        }
        ControlPanelDesiredAngleSignal = new Signal("Control Panel Desired Angle","deg");

    }

    public void update(){
        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000.0;

        if(Robot.isReal()){
            sampleSensors();
            if(Math.abs(ControlPanelMotor.getEncoder().getPosition()-desiredRotation_deg)<ErrorLimit.get()){
                rotationComplete = true;
                ControlPanelMotor.setClosedLoopRampRate(0);
            }
            if(!rotationComplete){
                controlPanelPID.setReference(desiredRotation_deg, ControlType.kPosition);
            }else{
                controlPanelPID.setReference(0,ControlType.kVoltage);
            }
            ControlPanelMotorCurrentSignal.addSample(sampleTimeMs, ControlPanelMotor.getOutputCurrent());
            ControlPanelActualAngleSignal.addSample(sampleTimeMs, ControlPanelMotor.getEncoder().getPosition());
        }
        ControlPanelDesiredAngleSignal.addSample(sampleTimeMs, desiredRotation_deg);
        //TODO
    }

    public void updateGains(boolean forceChange) {
        if(forceChange || haveCalsChanged()) {
            controlPanelPID.setP(kP.get());
            controlPanelPID.setI(kI.get());
            controlPanelPID.setD(kD.get());
            controlPanelPID.setFF(kFF.get());
            calsUpdated = true;
        }
    }

    public void sampleSensors(){
        if(calsUpdated) {
            kP.acknowledgeValUpdate();
            kI.acknowledgeValUpdate();
            kD.acknowledgeValUpdate();
            kFF.acknowledgeValUpdate();
            calsUpdated = false;
        }
    }

    private boolean haveCalsChanged() {
        return kP.isChanged() || kI.isChanged() || kD.isChanged() || kFF.isChanged();
    }

    public void sendRotationCommand(double desRotation_deg_in){
        if(Robot.isReal()){
            ControlPanelMotor.setClosedLoopRampRate(0.5);
        }
        rotationComplete = false;
        desiredRotation_deg += desRotation_deg_in; 
    }

    public void resetPos(){
        if(Robot.isReal()){
            desiredRotation_deg=ControlPanelMotor.getEncoder().getPosition();
        }else{
            desiredRotation_deg=0;
        }
    }

    public boolean isRotationCompleted(){
        return rotationComplete;
    }

    public void stopRotation(){
        if(Robot.isReal()){
            desiredRotation_deg=ControlPanelMotor.getEncoder().getPosition();
        }else{
            desiredRotation_deg=0;
        }
    }



}
