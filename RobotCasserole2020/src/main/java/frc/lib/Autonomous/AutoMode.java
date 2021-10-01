package frc.lib.Autonomous;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.robot.Drivetrain.Drivetrain;

public abstract class AutoMode {

    public String humanReadableName = "";

    public int idx = -1;

    public abstract void addStepsToSequencer(AutoSequencer seq);

    public abstract void setInitialPose(Drivetrain d);

    public AutoMode(){
        humanReadableName = this.getClass().getSimpleName();
    }
    
    public Pose2d getInitialPose(){
        return new Pose2d();
    }
}
