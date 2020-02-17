package frc.robot.VisionProc;

public abstract class VisionCamera {


    public abstract void update();

    /**
     * Returns the most recently seen target's X position in the image, converted to an angle from the robot.
     */
    public abstract double getTgtAngle();

    /**
     * Returns the estimated disatance to the target in ft.
     */
    public abstract double getTgtPositionX();

    /**
     * Returns the estimated disatance to the target in ft.
     */
    public abstract double getTgtPositionY();


    /* Returns the angle from the camera to the robot in degrees*/
    public abstract double getTgtGeneralAngle();

    /** Returnsn true if the target is visble to the camera */
    public abstract boolean isTgtVisible();

    /** Returns true if the vision camera is connected to the roboRIO */
    public abstract boolean isVisionOnline();

    /** Returns true when the image has settled and the pose reading can be trusted */
    public abstract boolean isTargetStable();

    /** Tells Camera to take a picture */
    public abstract void TakeAPicture();


}