package frc.robot.Autonomous;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.geometry.Twist2d;
import frc.lib.DataServer.Signal;
import frc.lib.SignalMath.MathyCircularBuffer;
import frc.robot.VisionProc.JeVoisInterface;
import frc.robot.LoopTiming;

class JevoisPoseCapture {


    /* All possible states for the state machine */
    public enum StateEnum {
        Inactive(0),   /* Inactive */
        SendJevoislatch(1),   /* Send jevois latch */
        waitForLatch(2),   /* wait for latln */
        sampleFromJEV(3),   /* sample from JCV */
        sampleReady(4), /* pose has been latched and is stable on read-back */
        visionError(5); /*Error occurred while reading pose */

        public final int value;

        private StateEnum(int value) {
            this.value = value;
        }
    }

    
    StateEnum curState;
    StateEnum prevState;

    boolean latchRequest = false;
    double seqStartTimestamp = 0;

    boolean stableTargetFound;
    MathyCircularBuffer tgtXPosBuf;
    MathyCircularBuffer tgtYPosBuf;
    MathyCircularBuffer tgtAngleBuf;

    double xTargetOffset;
    double yTargetOffset;
    double targetPositionAngle;

    int jeVoisPreLatchCount = 0;
    int jeVoisSampleCounter = 0;

    long prevFrameCounter = 0;

    Signal poseCapStateSig;
    Signal xTargetOffsetSig;
    Signal yTargetOffsetSig;
    Signal targetPositionAngleSig;

    final int NUM_AVG_JEVOIS_SAMPLES = 3;

    final double MAX_ALLOWABLE_DISTANCE_STANDARD_DEV = 50; //these are probably way too big, but can be tuned down on actual robot.
    final double MAX_ALLOWABLE_ANGLE_STANDARD_DEV = 100;
    final double VISION_TIMEOUT_SEC =3.0;

    public JevoisPoseCapture(){
        curState = StateEnum.Inactive;
        prevState = StateEnum.Inactive;

        poseCapStateSig = new Signal("Jevois Pose Capture State", "state");
        xTargetOffsetSig = new Signal("Jevois Pose Capture Tgt X Offset", "ft");
        yTargetOffsetSig = new Signal("Jevois Pose Capture Tgt Y Offset", "ft");
        targetPositionAngleSig = new Signal("Jevois Pose Capture Tgt Angle", "deg");
    }

    public void update(){
        //Main update loop
        StateEnum nextState = curState;

        boolean visionAvailable = JeVoisInterface.getInstance().isTgtVisible() && JeVoisInterface.getInstance().isVisionOnline();

        //Step 0 - save previous state
        prevState = curState;

        //Unconditional transition - go back to inactive when a latch is not longer required
        if(!latchRequest){
            nextState = StateEnum.Inactive;
        }

        //Do different things depending on what state you are in
        switch(curState){

            case Inactive:
                if(latchRequest && visionAvailable){
                    seqStartTimestamp = Timer.getFPGATimestamp();
                    nextState = StateEnum.SendJevoislatch;
                }
            break;

            case SendJevoislatch:
                jeVoisPreLatchCount = JeVoisInterface.getInstance().getLatchCounter();
                JeVoisInterface.getInstance().latchTarget();
                nextState = StateEnum.waitForLatch;
            break;

            case waitForLatch:
                if(JeVoisInterface.getInstance().getLatchCounter() > jeVoisPreLatchCount && visionAvailable){
                    //Jevois latched a new target. Start collecting samples
                    nextState = StateEnum.sampleFromJEV;
                } else if(!visionAvailable){                 
                    nextState = StateEnum.visionError;
                }
            break;

            case sampleFromJEV:
                if(Timer.getFPGATimestamp() > (seqStartTimestamp + VISION_TIMEOUT_SEC) ){
                    //took too long to get stable results from the Jevois. Fail.
                    nextState = StateEnum.visionError;
                } else {
                    if(visionAvailable){
                        long frameCounter = JeVoisInterface.getInstance().getFrameRXCount();
                        
                        if(frameCounter != prevFrameCounter){
                            //A new sample has come in from the vision camera
                            tgtXPosBuf.pushFront(JeVoisInterface.getInstance().getTgtPositionX());
                            tgtYPosBuf.pushFront(JeVoisInterface.getInstance().getTgtPositionY());
                            tgtAngleBuf.pushFront(JeVoisInterface.getInstance().getTgtAngle());
                            prevFrameCounter = frameCounter;
                            jeVoisSampleCounter++;

                            if(jeVoisSampleCounter >= NUM_AVG_JEVOIS_SAMPLES){
                                //We've got enough vision samples to start evaluating if the target is stable
                                if( (tgtXPosBuf.getStdDev() < MAX_ALLOWABLE_DISTANCE_STANDARD_DEV) &&
                                    (tgtYPosBuf.getStdDev() < MAX_ALLOWABLE_DISTANCE_STANDARD_DEV) &&
                                    (tgtAngleBuf.getStdDev() < MAX_ALLOWABLE_ANGLE_STANDARD_DEV) 
                                ){
                                    //Target is declared "Stable"
                                    xTargetOffset = tgtXPosBuf.getAverage();
                                    yTargetOffset = tgtYPosBuf.getAverage();
                                    targetPositionAngle = tgtAngleBuf.getAverage(); 
                                    nextState = StateEnum.sampleReady; 
                                } 
                            }                    
                        }
                    }
                }
            break;

            case sampleReady:
            break;

            case visionError:
            break;

            default:
                nextState = StateEnum.Inactive;
            break;
        }

        //Advance States
        curState = nextState;


        double sampleTimeMs = LoopTiming.getInstance().getLoopStartTimeSec()*1000;
        poseCapStateSig.addSample(sampleTimeMs, curState.value);
        xTargetOffsetSig.addSample(sampleTimeMs, xTargetOffset);
        yTargetOffsetSig.addSample(sampleTimeMs, yTargetOffset);
        targetPositionAngleSig.addSample(sampleTimeMs, targetPositionAngle);

    }

    public void setLatchRequested(boolean req_in){
        latchRequest = req_in;
    }

    public boolean latchAcquired(){
        return (curState == StateEnum.sampleReady);
    }

    public boolean visionErrorDetected(){
        return (curState == StateEnum.visionError);
    }

    public Twist2d getCamToTargetTwist(){
        return new Twist2d(xTargetOffset, yTargetOffset, targetPositionAngle);
    }




}