/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.BallHandling;

import frc.lib.Calibration.Calibration;
import frc.lib.Signal.Annotations.Signal;
import frc.robot.BallHandling.Conveyor.ConveyorOpMode;

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
    @Signal
    int ballsInConveyor = 0;
    @Signal
    BallHeight curBallHeight;
    BallHeight prevBallHeight;

    @Signal
    ConveyorDirection curConveyorDirection;
    ConveyorDirection prevConveyorDirection;

    @Signal
    double ballHeightIn;

    // Calibrations
    Calibration ballPresentDistThreshCal;
    Calibration ballApexDistThreshCal;


    private static BallCounter inst = null;

    public static synchronized BallCounter getInstance() {
        if (inst == null)
            inst = new BallCounter();
        return inst;

    }

    private BallCounter() {
        //Ensure sensor gets instantiated.
        BallDistanceSensor.getInstance();

        ballPresentDistThreshCal = new Calibration("Ball Counter Ball Present Distance Thresh Inches", 8.0);
        ballApexDistThreshCal = new Calibration("Ball Counter Ball Apex Distance Thresh Inches", 4.0);
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

    }

    public void setBallHeight() {
        ballHeightIn = BallDistanceSensor.getInstance().getDistance_in();
        if (ballHeightIn > ballPresentDistThreshCal.get()) {
            curBallHeight = BallHeight.Empty;
        } else if (ballHeightIn < ballPresentDistThreshCal.get() && ballHeightIn > ballApexDistThreshCal.get()) {
            curBallHeight = BallHeight.ShortButPresent;
        } else {
            curBallHeight = BallHeight.Apex;
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
