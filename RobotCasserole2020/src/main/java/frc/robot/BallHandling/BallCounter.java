/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.BallHandling;

import org.eclipse.jetty.util.HttpCookieStore.Empty;

import frc.lib.Calibration.Calibration;
import frc.robot.LoopTiming;
import frc.robot.BallHandling.Conveyor.ConveyorOpMode;
import frc.lib.DataServer.Signal;

public class BallCounter {

    public enum BallHeight {
        Empty(0), ShortButPresent(1), Apex(2);

        public final int value;

        private BallHeight(int value) {
            this.value = value;
        }
    }

    public enum ConveyorDirection {
        Stationary(0), Forward(1), Reverse(2);

        public final int value;

        private ConveyorDirection(int value) {
            this.value = value;
        }
    }

    // State Data
    int ballsInConveyor = 0;
    BallHeight curBallHeight;
    BallHeight prevBallHeight;

    ConveyorDirection curConveyorDirection;
    ConveyorDirection prevConveyorDirection;

    Double ballHeightIn;

    // Calibrations
    Calibration ballMinThicknessCal;
    Calibration ballMaxThicknessCal;

    // Signals
    Signal ballCountSig;
    Signal howFarFromSensorSig;
    Signal whatHeightWeSaySig;

    private static BallCounter inst = null;

    public static synchronized BallCounter getInstance() {
        if (inst == null)
            inst = new BallCounter();
        return inst;

    }

    private BallCounter() {
        //Ensure sensor gets instantiated.
        BallDistanceSensor.getInstance();

        ballMinThicknessCal = new Calibration("Ball Counter Empty-Short Thresh Inches", 6);
        ballMaxThicknessCal = new Calibration("Ball Counter Short-Apex Thresh Inches", 2);

        ballCountSig = new Signal("Ball Counter Ball Count", "count");
        howFarFromSensorSig = new Signal("Ball Counter Sensor Distance", "in");
        whatHeightWeSaySig = new Signal("Ball Counter Ball Height State", "heightState");

    }

    public void update() {
        BallDistanceSensor.getInstance().update();
        setBallHeight();
        setConveyorDirection();

        if (curConveyorDirection == ConveyorDirection.Forward) {
            if (curBallHeight != prevBallHeight) {
                if (curBallHeight == BallHeight.Apex) {
                    ballsInConveyor += 1;
                }

            } else if (curConveyorDirection == ConveyorDirection.Reverse) {
                if (curBallHeight != prevBallHeight) {
                    if (curBallHeight == BallHeight.Apex) {
                        ballsInConveyor -= 1;
                    }
                }
            }
        }
        prevBallHeight = curBallHeight;

        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000;
        ballCountSig.addSample(sampleTimeMS, ballsInConveyor);
        howFarFromSensorSig.addSample(sampleTimeMS, ballHeightIn);
        whatHeightWeSaySig.addSample(sampleTimeMS, curBallHeight.value);

    }

    public void setBallHeight() {
        ballHeightIn = BallDistanceSensor.getInstance().getDistance_in();
        if (ballHeightIn > ballMaxThicknessCal.get()) {
            curBallHeight = BallHeight.Apex;
        } else if (ballHeightIn < ballMaxThicknessCal.get() && ballHeightIn > ballMinThicknessCal.get()) {
            curBallHeight = BallHeight.ShortButPresent;
        } else {
            curBallHeight = BallHeight.Empty;
        }
    }

    public void setConveyorDirection() {
        ConveyorOpMode convOpMode = Conveyor.getInstance().getOpMode();
        if (convOpMode == ConveyorOpMode.AdvanceFromHopper || convOpMode == ConveyorOpMode.AdvanceToShooter
                || convOpMode == ConveyorOpMode.InjectIntoShooter) {
            curConveyorDirection = ConveyorDirection.Forward;
        } else if (convOpMode == ConveyorOpMode.Reverse) {
            curConveyorDirection = ConveyorDirection.Reverse;
        }

    }

    public int getBallCount() {
        return ballsInConveyor;
    }

    public boolean isBallPresent() {
        return curBallHeight != BallHeight.Empty;
    }
}
