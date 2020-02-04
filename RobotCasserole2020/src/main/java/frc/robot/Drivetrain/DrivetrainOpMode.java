package frc.robot.Drivetrain;

public enum DrivetrainOpMode {
    kOpenLoop(0), kClosedLoopVelocity(1), kGyroLock(2), kTurnToAngle(3);


    public final int value;

    private DrivetrainOpMode(int value) {
        this.value = value;
    }
            
    public int toInt(){
        return this.value;
    }
}
