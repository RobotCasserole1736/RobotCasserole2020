package frc.robot;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Spark;
import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Signal;

public class IntakeControl {

	Spark intakeMotor;
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
		intakeMotor = new Spark(RobotConstants.INTAKE_MOTOR);
		intakeSolenoid = new Solenoid(RobotConstants.INTAKE_SOLENOID_FWD);
		
		intakeSpeedCmd = new Calibration("Intake Speed Cmd", 0.5);
		ejectSpeedCmd = new Calibration("Eject Speed Cmd", -0.5); 

		posState = IntakePosition.Retracted; 
		spdState = IntakeSpeed.Stop;

		posStateSig = new Signal("Intake Position State", "state");
		spdStateSig = new Signal("Intake Speed State", "state");
		motorCurrentSig = new Signal("Intake Motor Current", "A");
		//TODO
	}

	public void update(){
		switch(posState){
			case Retracted:
				intakeSolenoid.set(INTAKE_RETRACTED);
			break;
			case Extended:
				intakeSolenoid.set(INTAKE_EXTENDED);
			break;
		}
		switch(spdState){
			case Eject:
				intakeMotor.set(ejectSpeedCmd.get());
			break;
			case Stop:
				intakeMotor.set(0);
			break;
			case Intake:
				intakeMotor.set(intakeSpeedCmd.get());
			break; 
		}
		//TODO
	}

	public void setDesiredPosition(IntakePosition des_pos){
		posState = des_pos;
	}

	public IntakePosition getActualPosition(){
		return IntakePosition.Retracted; //TODO - make it return something reasonable
	}

	public void setDesiredSpeed(IntakeSpeed des_spd){
		spdState = des_spd;
	}		//TODO
}