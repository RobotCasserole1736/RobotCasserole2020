package frc.robot;

public class IntakeControl{

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
		//TODO
	}

	public void update(){
		//TODO
	}

	public void setDesiredPosition(IntakePosition des_pos){
		//TODO
	}

	public IntakePosition getActualPosition(){
		return IntakePosition.Retracted; //TODO - make it return something reasonable
	}

	public void setDesiredSpeed(IntakeSpeed des_spd){
		//TODO
	}



}