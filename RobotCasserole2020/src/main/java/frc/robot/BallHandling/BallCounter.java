/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.BallHandling;

import org.eclipse.jetty.util.HttpCookieStore.Empty;

import frc.lib.Calibration.Calibration;
import frc.robot.BallHandling.Conveyor.ConveyerOpMode;
import frc.lib.DataServer.Signal;




public class BallCounter {

    double sampleTimeMS = 20;
    public enum BallHeight {
        Empty(0),
        ShortButPresent(1),
        Apex(2);

        public final int value;

        private BallHeight(int value) {
            this.value = value;
        }
    }
        public enum ConveyorDirection {
            Stationary(0),
            Forward(1),
            Reverse(2);

    
            public final int value;
    
            private ConveyorDirection(int value) {
                this.value = value;
            }
        }

    //State Data    
    int ballsInConveyor = 0;
    BallHeight curBallHeight;
    BallHeight prevBallHeight;

    ConveyorDirection curConveyorDirection;
    ConveyorDirection prevConveyorDirection;

    Double ballHeightIn;


    //Calibrations
    Calibration ballMinThicknessCal;
    Calibration ballMaxThicknessCal;
    
    //Signals
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
        
        ballMinThicknessCal = new Calibration("Ball Is too Short", 1);
        ballMaxThicknessCal = new Calibration("Thick Side of Ball", 5);
        
        ballCountSig = new Signal("How many balls are in the Conveyor", "balls");
        howFarFromSensorSig = new Signal("Distance From Ball to Sensor", "inches");
        whatHeightWeSaySig = new Signal("What Are We Calling The Measurement", "NameOfState");

    }
    public void update() {
        setBallHeight();
        setConveyorDirection();
        
        if(curConveyorDirection == ConveyorDirection.Forward) {
            if(curBallHeight != prevBallHeight) {
                if(curBallHeight == BallHeight.Apex){
                    ballsInConveyor +=1 ;
            }
                
        }else if(curConveyorDirection == ConveyorDirection.Reverse) {
                    if(curBallHeight != prevBallHeight) {
                        if(curBallHeight == BallHeight.Apex) {
                            ballsInConveyor -= 1;
                    }
                }
        }   
        }
        prevBallHeight = curBallHeight;
        
        ballCountSig.addSample(sampleTimeMS, ballsInConveyor);
        howFarFromSensorSig.addSample(sampleTimeMS, ballHeightIn);
        whatHeightWeSaySig.addSample(sampleTimeMS, curBallHeight.value);
  
    }
    public void setBallHeight() {
        ballHeightIn = BallDistanceSensor.getInstance().getDistance_in();
        if(ballHeightIn > ballMaxThicknessCal.get()) {
            curBallHeight = BallHeight.Apex;
        } else if (ballHeightIn < ballMaxThicknessCal.get() && ballHeightIn > ballMinThicknessCal.get()) {
            curBallHeight = BallHeight.ShortButPresent;
        }else {
            curBallHeight = BallHeight.Empty;
        }
    }
    public void setConveyorDirection() {
        ConveyerOpMode convOpMode = Conveyor.getInstance().getOpMode();
        if(convOpMode == ConveyerOpMode.AdvanceFromHopper || convOpMode == ConveyerOpMode.AdvanceToShooter || convOpMode == ConveyerOpMode.InjectIntoShooter) {
            curConveyorDirection = ConveyorDirection.Forward;
        }else if(convOpMode == ConveyerOpMode.Reverse) {
            curConveyorDirection = ConveyorDirection.Reverse;
        }

    }


    public int getBallCount() {
        
        return ballsInConveyor;
    }
}

