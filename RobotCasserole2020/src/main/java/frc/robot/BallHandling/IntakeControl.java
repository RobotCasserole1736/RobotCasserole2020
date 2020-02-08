package frc.robot.BallHandling;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.Solenoid;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;
import edu.wpi.first.wpilibj.Timer;

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

	IntakePosition posState;
	IntakeSpeed spdState;

	public final boolean INTAKE_RETRACTED = false;
	public final boolean INTAKE_EXTENDED = true;

	Signal posStateSig;
	Signal spdStateSig;
	Signal motorCurrentSig;
	Signal intkSpdCmdSig;

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
		Intake(1);

        public final int value;
        private IntakeSpeed(int value) {
            this.value = value;
        }
    }


	private IntakeControl(){
		intakeMotor = new CANSparkMax(RobotConstants.INTAKE_MOTOR_CAN_ID, MotorType.kBrushed);
		intakeSolenoid = new Solenoid(RobotConstants.INTAKE_SOLENOID_PCM_PORT);

		intakeMotor.setSmartCurrentLimit(30); //30A limit
		
		intakeSpeedCmd = new Calibration("Intake Speed Cmd", 0.5);
		ejectSpeedCmd = new Calibration("Eject Speed Cmd", -0.5); 

		posState = IntakePosition.Retracted; 
		spdState = IntakeSpeed.Stop;

		posStateSig = new Signal("Intake Position State", "state");
		spdStateSig = new Signal("Intake Speed State", "state");
		motorCurrentSig = new Signal("Intake Motor Current", "A");
		intkSpdCmdSig = new Signal("Intake Speed Command", "cmd");
	}

	public void update(){
		switch(posState){
			case Retracted:
				intakeSolenoid.set(INTAKE_RETRACTED);
				prevTimeRet = Timer.getFPGATimestamp();
			break;
			case Extended:
				intakeSolenoid.set(INTAKE_EXTENDED);
				prevTimeExt = Timer.getFPGATimestamp();
			break;
		}
		switch(spdState){
			case Eject:
				intakeMotor.set(ejectSpeedCmd.get());
				intkCommand = 0;
			break;
			case Stop:
				intakeMotor.set(0);
				intkCommand = 0;
			break;
			case Intake:
				intakeMotor.set(intakeSpeedCmd.get());
				intkCommand = intakeSpeedCmd.get();
			break; 
		}
		
		double sampleTimeMs = (LoopTiming.getInstance().getLoopStartTimeSec() * 1000);
		posStateSig.addSample(sampleTimeMs, posState.value);
		spdStateSig.addSample(sampleTimeMs, spdState.value);
		motorCurrentSig.addSample(sampleTimeMs, intakeMotor.getOutputCurrent());
		intkSpdCmdSig.addSample(sampleTimeMs, intkCommand);

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