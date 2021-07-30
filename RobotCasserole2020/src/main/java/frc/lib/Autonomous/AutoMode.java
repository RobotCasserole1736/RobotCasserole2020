package frc.lib.Autonomous;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import frc.lib.AutoSequencer.AutoSequencer;

public abstract class AutoMode {

    public String humanReadableName = "";

    public int idx = -1;

    public abstract void addStepsToSequencer(AutoSequencer seq);
    
    public Pose2d getInitialPose(){
        //return Constants.DFLT_START_POSE;
        return new Pose2d();
    }
}
