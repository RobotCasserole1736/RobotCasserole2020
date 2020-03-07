package frc.robot.BallHandling;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.Solenoid;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import frc.robot.Robot;
import frc.robot.RobotConstants;
import edu.wpi.first.wpilibj.Timer;
import com.revrobotics.CANSparkMax.IdleMode;

public class IntakeControl {

	double prevTimeExt;
	double prevTimeRet;
	double endTimeExt;
	double endTimeRet;
	double duration_s;

	double intkCommand = 0;

	CANSparkMax intakeMotor;
	Solenoid intakeSolenoid;

	Calibration intakeSpeedCmd;
	Calibration ejectSpeedCmd;
	Calibration intakeSlowCmd;

	IntakePosition posState;
	IntakeSpeed spdState;

	public final boolean INTAKE_RETRACTED = false;
	public final boolean INTAKE_EXTENDED = true;

	Signal posStateSig;
	Signal spdStateSig;
	Signal motorCurrentSig;
	Signal intkSpdCmdSig;
	Signal intkSolCmdSig;
	Signal intkSpeedSig;
	Signal intkFaultSig;

    private static IntakeControl instance = null;
	public static synchronized IntakeControl getInstance() {
		if(instance == null)
		instance = new IntakeControl();
		return instance;
	}
	
    /* Allowable intake positions*/
    public enum IntakePosition {
        Retracted(0),   
        Extended(1);

        public final int value;
        private IntakePosition(int value) {
            this.value = value;
        }
	}
	
    /* Allowable intake speeds*/
    public enum IntakeSpeed {
        Eject(-1),   
		Stop(0),
		Intake(1),
		IntakeButSlowly(2);

        public final int value;
        private IntakeSpeed(int value) {
            this.value = value;
        }
    }


	private IntakeControl(){
		if(Robot.isReal()){
			//No REV support for sim :(
			intakeMotor = new CANSparkMax(RobotConstants.INTAKE_MOTOR_CAN_ID, MotorType.kBrushless);
			intakeMotor.restoreFactoryDefaults();
			intakeMotor.setInverted(true);
			intakeMotor.setSmartCurrentLimit(65); //Prevent the magic smoke
			intakeMotor.setIdleMode(IdleMode.kCoast);
			intakeMotor.setCANTimeout(RobotConstants.CAN_TIMEOUT);
			intakeMotor.burnFlash();
        
		}

		intakeSolenoid = new Solenoid(RobotConstants.INTAKE_SOLENOID_PCM_PORT);


		
		intakeSpeedCmd = new Calibration("Intake Speed Cmd", 1);
		intakeSlowCmd = new Calibration("Intake Slow Speed Cmd", 0.2);
		ejectSpeedCmd = new Calibration("Intake Eject Speed Cmd", -0.5); 

		posState = IntakePosition.Retracted; 
		spdState = IntakeSpeed.Stop;

		posStateSig = new Signal("Intake Position State", "state");
		spdStateSig = new Signal("Intake Speed State", "state");
		motorCurrentSig = new Signal("Intake Motor Current", "A");
		intkSpdCmdSig = new Signal("Intake Speed Command", "cmd");
		intkSolCmdSig = new Signal("Intake Solenoid Command", "bool");
		intkSpeedSig = new Signal("Intake Motor Speed", "RPM");
		intkFaultSig = new Signal("Intake Fault", "bits");
	}

	public void update(){
		boolean intake_solenoid_cmd = false;

		switch(posState){
			case Retracted:
				intake_solenoid_cmd = INTAKE_RETRACTED;
				prevTimeRet = Timer.getFPGATimestamp();
			break;
			case Extended:
				intake_solenoid_cmd = INTAKE_EXTENDED;
				prevTimeExt = Timer.getFPGATimestamp();
			break;
		}

		switch(spdState){
			case Eject:
				intkCommand = ejectSpeedCmd.get();
			break;
			case Stop:
				intkCommand = 0;
			break;
			case Intake:
				intkCommand = intakeSpeedCmd.get();
			break; 
			case IntakeButSlowly:
				intkCommand = intakeSlowCmd.get();
			break;
		}

		double sampleTimeMs = (LoopTiming.getInstance().getLoopStartTimeSec() * 1000);

		intakeSolenoid.set(intake_solenoid_cmd);
		if(Robot.isReal()){
			intakeMotor.set(intkCommand);
			motorCurrentSig.addSample(sampleTimeMs, intakeMotor.getOutputCurrent());
			intkSpeedSig.addSample(sampleTimeMs, intakeMotor.getEncoder().getVelocity());
			intkFaultSig.addSample(sampleTimeMs, intakeMotor.getFaults());
		}
		
		posStateSig.addSample(sampleTimeMs, posState.value);
		spdStateSig.addSample(sampleTimeMs, spdState.value);
		intkSpdCmdSig.addSample(sampleTimeMs, intkCommand);
		intkSolCmdSig.addSample(sampleTimeMs, intake_solenoid_cmd);


	}

	public void setPosMode(IntakePosition des_pos){
		posState = des_pos;
	}


	public void setSpeedMode(IntakeSpeed des_spd){
		spdState = des_spd;
	}

	public boolean isIntakeExtended(double duration_s_in){
		boolean extended = false;
		duration_s = duration_s_in;
		endTimeExt = Timer.getFPGATimestamp() - prevTimeExt;
		if(endTimeExt >= duration_s){
			extended = true;
		}else if(endTimeExt < duration_s){
			extended = false;
		} 
		return extended;
		//code shouldn't be used right now but may change later
	}
	
	public boolean isIntakeRaise(double duration_s_in){
		boolean raised = false;
		duration_s = duration_s_in;
		endTimeRet = Timer.getFPGATimestamp() - prevTimeRet;
		if(endTimeRet >= duration_s){
			raised = true;
		}else if(endTimeRet < duration_s){
			raised = false;
		}
		return raised;
		//code shouldn't be used right now but may change later
	}
}