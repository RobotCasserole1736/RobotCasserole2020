package frc.robot.Autonomous;

import edu.wpi.first.wpilibj.DriverStation;
import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Util.CrashTracker;
import frc.lib.WebServer.CasseroleDriverView;
import frc.robot.Autonomous.Events.AutoEventAltSteakDriveFwdPtOne;
import frc.robot.Autonomous.Events.AutoEventAltSteakDriveFwdPtThree;
import frc.robot.Autonomous.Events.AutoEventAltSteakDriveFwdPtTwo;
import frc.robot.Autonomous.Events.AutoEventBackUpAFoot;
import frc.robot.Autonomous.Events.AutoEventBackUpFromBallThief;
import frc.robot.Autonomous.Events.AutoEventBackUpThreeFeet;
import frc.robot.Autonomous.Events.AutoEventCitrusSteakA;
import frc.robot.Autonomous.Events.AutoEventCitrusSteakB;
import frc.robot.Autonomous.Events.AutoEventCollectSteak;
import frc.robot.Autonomous.Events.AutoEventCollectSteakPt2;
import frc.robot.Autonomous.Events.AutoEventDriveForTime;
import frc.robot.Autonomous.Events.AutoEventDriveToShootFromSteakCollect;
import frc.robot.Autonomous.Events.AutoEventNoStealSteakA;
import frc.robot.Autonomous.Events.AutoEventNoStealSteakB;
import frc.robot.Autonomous.Events.AutoEventStopRobot;
import frc.robot.Autonomous.Events.AutoEventDriveToBallThief;
import frc.robot.Autonomous.Events.AutoEventPathPlanTest;
import frc.robot.Autonomous.Events.AutoEventReversePathPlanTest;
import frc.robot.Autonomous.Events.AutoEventShootClose;
import frc.robot.Autonomous.Events.AutoEventShootFar;
import frc.robot.Autonomous.Events.AutoEventTurn;
import frc.robot.Autonomous.Events.AutoEventTurnToVisionTarget;
import frc.robot.Autonomous.Events.AutoEventWait;
import frc.robot.Drivetrain.Drivetrain;
import frc.robot.HumanInterface.DriverController;
import frc.robot.ShooterControl.ShooterControl;
import frc.robot.ShooterControl.ShooterControl.ShooterRunCommand;



/*
 *******************************************************************************************
 * Copyright (C) 2020 FRC Team 1736 Robot Casserole - www.robotcasserole.org
 *******************************************************************************************
 *
 * This software is released under the MIT Licence - see the license.txt
 *  file in the root of this repo.
 *
 * Non-legally-binding statement from Team 1736:
 *  Thank you for taking the time to read through our software! We hope you
 *    find it educational and informative! 
 *  Please feel free to snag our software for your own use in whatever project
 *    you have going on right now! We'd love to be able to help out! Shoot us 
 *    any questions you may have, all our contact info should be on our website
 *    (listed above).
 *  If you happen to end up using our software to make money, that is wonderful!
 *    Robot Casserole is always looking for more sponsors, so we'd be very appreciative
 *    if you would consider donating to our club to help further STEM education.
 */


public class Autonomous {

    /* All possible autonomously performed routines */
    public enum AutoMode {
        DoNothing(0),   
        DriveFwd(1),  
        ShootOnly(2),  
        VisionAlignShoot(3),   
        CloseVisionAlignShoot(4),
        BallThief(5),
        Steak(6),
        OurSideSteak(7),
        CitrusSteak(8),
        NoStealSteak(9),
        VisionAlignOnly(10),
        SWTest(11),
        Inactive(-1); 

        public final int value;

        private AutoMode(int value) {
            this.value = value;
        }
    }
    
    /* Singleton infratructure*/
    private static Autonomous inst = null;
    public static synchronized Autonomous getInstance() {
        if (inst == null)
            inst = new Autonomous();
        return inst;
    }

    AutoSequencer seq;

    double delayTime_s= 0.0;
    double delayTime_s_prev= 0.0;
    AutoMode modeCmd = AutoMode.Inactive;
    AutoMode modeCmdPrev = AutoMode.Inactive;
    AutoMode actualMode;
    String autoModeName = "";

    public static final String[] ACTION_MODES =  new String[]{"Do Nothing", 
                                                              "Drive Forward", 
                                                              "Shoot Only", 
                                                              "Vision Align Shoot", 
                                                              "Close Vision Align Shoot",
                                                              "Ball Thief",                                              
                                                              "Steak",
                                                              "Our Side Steak",
                                                              "Citrus Steak",
                                                              "No Steal Steak",
                                                              "SW TEAM TEST ONLY"};

    public static final String[] DELAY_OPTIONS = new String[]{"0s", 
                                                              "3s", 
                                                              "6s",
                                                              "9s",
                                                              "12s"};


    private Autonomous(){
        seq = new AutoSequencer("Autonomous");
    }

    /* This should be called periodically in Disabled, and once in auto init */
    public void sampleDashboardSelector(){
		String actionStr    = CasseroleDriverView.getAutoSelectorVal("Action");
		String delayTimeStr = CasseroleDriverView.getAutoSelectorVal("Delay");
		autoModeName = actionStr + " delay by " + delayTimeStr;
		
		//Map delay times from selelctor to actual quantities
		if(delayTimeStr.compareTo(DELAY_OPTIONS[0]) == 0) { //No Delay
			delayTime_s = 0.0;
		} else if (delayTimeStr.compareTo(DELAY_OPTIONS[1]) == 0) { 
			delayTime_s = 3.0;
		} else if (delayTimeStr.compareTo(DELAY_OPTIONS[2]) == 0) { 
			delayTime_s = 6.0;
		} else if (delayTimeStr.compareTo(DELAY_OPTIONS[3]) == 0) { 
			delayTime_s = 9.0;
		} else if (delayTimeStr.compareTo(DELAY_OPTIONS[4]) == 0) { 
			delayTime_s = 12.0;
        }
        
        // Map Auto mode selector values to the enum
		if(actionStr.compareTo(ACTION_MODES[0]) == 0) {
			modeCmd = AutoMode.DoNothing;
		} else if (actionStr.compareTo(ACTION_MODES[1]) == 0) { 
			modeCmd = AutoMode.DriveFwd;
		} else if (actionStr.compareTo(ACTION_MODES[2]) == 0) { 
			modeCmd = AutoMode.ShootOnly;
		} else if (actionStr.compareTo(ACTION_MODES[3]) == 0) { 
            modeCmd = AutoMode.VisionAlignShoot;
		} else if (actionStr.compareTo(ACTION_MODES[4]) == 0) { 
            modeCmd = AutoMode.CloseVisionAlignShoot;
        } else if (actionStr.compareTo(ACTION_MODES[5]) == 0){
            modeCmd = AutoMode.BallThief;
        } else if (actionStr.compareTo(ACTION_MODES[6]) == 0) { 
            modeCmd = AutoMode.Steak;
        } else if (actionStr.compareTo(ACTION_MODES[7]) == 0) { 
			modeCmd = AutoMode.OurSideSteak;
		} else if (actionStr.compareTo(ACTION_MODES[8]) == 0) { 
			modeCmd = AutoMode.CitrusSteak;
        } else if (actionStr.compareTo(ACTION_MODES[9]) == 0){
            modeCmd = AutoMode.NoStealSteak;
        } else if (actionStr.compareTo(ACTION_MODES[10]) == 0) { 
            modeCmd = AutoMode.SWTest;
        } else {
            modeCmd = AutoMode.Inactive;
        }

        loadSequencer(true);
    }

    boolean visionAlignOnlyButtonReleased = false;
    boolean visionAlignShootButtonReleased = false;


    public void sampleOperatorCommands(){
        delayTime_s = 0; //Never delay while operator triggers auto modes

        if(DriverController.getInstance().getAutoAlignAndShootCmd()){
            visionAlignOnlyButtonReleased = true; // assume opposite button released
            if(visionAlignShootButtonReleased==true){
                modeCmd = AutoMode.VisionAlignShoot;
                autoModeName = "Driver Commanded Vision Align And Shoot";
                visionAlignShootButtonReleased = false;
            } else {
                //Do Nothing until driver releases the button
            }
        } else if(DriverController.getInstance().getAutoAlignCmd()){
            visionAlignShootButtonReleased = true; // assume opposite button released
            if(visionAlignOnlyButtonReleased==true){
                modeCmd = AutoMode.VisionAlignOnly;
                autoModeName = "Driver Commanded Vision Align Only";
                visionAlignOnlyButtonReleased = false;
            } else {
                //Do Nothing until driver releases the button
            }
        } else if(DriverController.getInstance().getAutoAlignAndShootCloseCmd()){
            visionAlignOnlyButtonReleased = true; // assume opposite button released
            if(visionAlignShootButtonReleased==true){
                modeCmd = AutoMode.CloseVisionAlignShoot;
                autoModeName = "Driver Commanded Vision Align And Shoot";
                visionAlignShootButtonReleased = false;
            } else {
                //Do Nothing until driver releases the button
            }
        } else {
            visionAlignOnlyButtonReleased = true;
            visionAlignShootButtonReleased = true;
            modeCmd = AutoMode.Inactive;
            autoModeName = "Inactive";
        }


        if(modeCmd != modeCmdPrev){
            //Load/run the command imedeately.
            loadSequencer(false);
            startSequencer();
        }

    }


    public void startSequencer(){
        if(actualMode != AutoMode.Inactive){
            seq.start();
        }
    }

    public void loadSequencer(boolean resetPose){
        
        if(modeCmd != modeCmdPrev || delayTime_s_prev != delayTime_s){

            CrashTracker.logGenericMessage("Initing new auto routine " + autoModeName);

            //Ensure everything on the robot is stopped
            seq.stop();
            Drivetrain.getInstance().setOpenLoopCmd(0, 0);
            ShooterControl.getInstance().setRun(ShooterRunCommand.Stop);
            seq.clearAllEvents();
            

            // Tack on the very first "wait" event 
            if(delayTime_s != 0.0){
                seq.addEvent(new AutoEventWait(delayTime_s));
            }

            // If desired, make sure we set our robot's initial position. This should really only be for
            //   auto, when we have an routine with a known & fixed start location.
            if(resetPose){
                switch(modeCmd){
                    case ShootOnly:
                        Drivetrain.getInstance().setInitialPose(-8, 10, 270.0);
                    break;
                    case BallThief:
                        Drivetrain.getInstance().setInitialPose(10, 11.5, 90);
                    break;
                    case Steak:
                        Drivetrain.getInstance().setInitialPose(10, 11.5, 90);
                    break;
                    case OurSideSteak:
                        Drivetrain.getInstance().setInitialPose(10, 11.5, 90);
                    break;
                    case CitrusSteak:
                        Drivetrain.getInstance().setInitialPose(10, 11.5, 90);
                    break;
                    case NoStealSteak:
                        Drivetrain.getInstance().setInitialPose(-10, 11.5, 90);
                    break;
                }
            }

            //Queue up the auto sequence manager with the desired events
            switch(modeCmd){
                case DoNothing:
                    //Empty sequencer - no one here but us chickens.
                break;

                case DriveFwd:
                    seq.addEvent(new AutoEventDriveForTime(0.5, 0.25));
                break;

                case SWTest:
                    // seq.addEvent(new AutoEventPathPlanTest());
                    // seq.addEvent(new AutoEventReversePathPlanTest());
                    // seq.addEvent(new AutoEventStopRobot());

                    //seq.addEvent(new AutoEventTurn(90));
                    //seq.addEvent(new AutoEventBackUpThreeFeet());

                    seq.addEvent(new AutoEventTurnToVisionTarget());
                    seq.addEvent(new AutoEventShootClose(15.0, 5));

                break;

                case ShootOnly:
                    seq.addEvent(new AutoEventShootFar(15.0,5));
                break;

                case VisionAlignOnly:
                    seq.addEvent(new AutoEventStopRobot());
                    seq.addEvent(new AutoEventTurnToVisionTarget());
                break;

                case VisionAlignShoot:
                    seq.addEvent(new AutoEventTurnToVisionTarget());
                    if(DriverStation.getInstance().isAutonomous()){
                        seq.addEvent(new AutoEventShootFar(15.0,5));
                    } else {
                        seq.addEvent(new AutoEventShootFar(150.0,100));
                    }

                break;

                case CloseVisionAlignShoot:
                    seq.addEvent(new AutoEventTurnToVisionTarget());
                    if(DriverStation.getInstance().isAutonomous()){
                        seq.addEvent(new AutoEventShootClose(15.0,5));
                    } else {
                        seq.addEvent(new AutoEventShootClose(150.0,100));
                    }

                break;

                case BallThief:
                    Drivetrain.getInstance().setInitialPose(10, 11.5, 90);
                    seq.addEvent(new AutoEventDriveToBallThief(4.0)); //Time is for intk, which is included
                    seq.addEvent(new AutoEventBackUpFromBallThief(4.0,1.0)); //Time is for shoot prep, which is included
                    seq.addEvent(new AutoEventTurn(6));
                    seq.addEvent(new AutoEventTurnToVisionTarget());
                    seq.addEvent(new AutoEventShootClose(4.0, 5));
                break;

                case Steak:
                    Drivetrain.getInstance().setInitialPose(10, 11.5, 90);
                    seq.addEvent(new AutoEventDriveToBallThief(4.0)); //Time is for intk, which is included
                    seq.addEvent(new AutoEventBackUpFromBallThief(4.0,1.0)); //Time is for shoot prep, which is included
                    seq.addEvent(new AutoEventTurn(6));
                    seq.addEvent(new AutoEventTurnToVisionTarget());
                    seq.addEvent(new AutoEventShootClose(3.0, 5));
                    seq.addEvent(new AutoEventCollectSteak(4.0)); //Time is for intk, which is included
                    seq.addEvent(new AutoEventTurn(25));
                    seq.addEvent(new AutoEventCollectSteakPt2(4.0));
                    seq.addEvent(new AutoEventTurn(15));
                    seq.addEvent(new AutoEventTurnToVisionTarget());
                    seq.addEvent(new AutoEventShootClose(10.0, 5));
                break;
                case CitrusSteak:
                    Drivetrain.getInstance().setInitialPose(10, 11.5, 90);
                    seq.addEvent(new AutoEventCitrusSteakA(4.0));
                    seq.addEvent(new AutoEventTurn(114));
                    seq.addEvent(new AutoEventTurnToVisionTarget());
                    seq.addEvent(new AutoEventShootClose(3.0,5));
                    // seq.addEvent(new AutoEventTurn(-24));
                    // seq.addEvent(new AutoEventCitrusSteakB(4.0));
                    // seq.addEvent(new AutoEventShootFar(3.0,5));
                    //seq.addEvent(new AutoEventTurnToVisionTarget());
                    //seq.addEvent(new AutoEventShootFar(3.0,5));
                    //seq.addEvent(new AutoEventShootFar(8.0,5));
                break;
                case OurSideSteak:
                    Drivetrain.getInstance().setInitialPose(10, 11.5, 90);
                    seq.addEvent(new AutoEventDriveToBallThief(4.0)); //Time is for intk, which is included
                    seq.addEvent(new AutoEventBackUpFromBallThief(4.0,1.0)); //Time is for shoot prep, which is included
                    seq.addEvent(new AutoEventTurnToVisionTarget());
                    seq.addEvent(new AutoEventShootFar(5.0,5));
                    seq.addEvent(new AutoEventTurn(110));
                    seq.addEvent(new AutoEventAltSteakDriveFwdPtOne(3.0)); //Time is for intk, which is included
                    seq.addEvent(new AutoEventBackUpThreeFeet());
                    seq.addEvent(new AutoEventTurn(25));
                    seq.addEvent(new AutoEventAltSteakDriveFwdPtTwo(1.0)); //Time is for intk, which is included
                    seq.addEvent(new AutoEventBackUpThreeFeet());
                    seq.addEvent(new AutoEventTurn(25));
                    seq.addEvent(new AutoEventAltSteakDriveFwdPtTwo(1.0)); //Time is for intk, which is included
                    seq.addEvent(new AutoEventBackUpAFoot());
                    seq.addEvent(new AutoEventTurn(120));
                    seq.addEvent(new AutoEventAltSteakDriveFwdPtThree(3.0)); //Time is for shoot prep, which is included
                    seq.addEvent(new AutoEventTurnToVisionTarget());
                    seq.addEvent(new AutoEventShootFar(5.0,5));
                break;
                case NoStealSteak:
                    Drivetrain.getInstance().setInitialPose(-10, 11.5, 90);
                    seq.addEvent(new AutoEventNoStealSteakA(4.0));
                    seq.addEvent(new AutoEventTurn(50));
                    seq.addEvent(new AutoEventShootClose(3.0, 5));
                    seq.addEvent(new AutoEventNoStealSteakB(4.0));
                    seq.addEvent(new AutoEventShootFar(3.0,3));
                break;
            }
            modeCmdPrev = modeCmd;
            actualMode = modeCmd;
            delayTime_s_prev = delayTime_s;
        }
    }


    /* This should be called periodically, always */
    public void update(){

        if(actualMode != AutoMode.Inactive){
            seq.update();
        }   
    }

    /* Should be called when returning to disabled to stop everything */
    public void reset(){
        modeCmd = AutoMode.Inactive;
        loadSequencer(false);
    }

    public boolean isActive(){
        return (seq.isRunning() && actualMode != AutoMode.Inactive);
    }
}