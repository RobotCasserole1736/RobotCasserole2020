package frc.robot.Autonomous;

import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Util.CrashTracker;
import frc.lib.WebServer.CasseroleDriverView;
import frc.robot.Autonomous.Events.AutoEventBackUpFromBallThief;
import frc.robot.Autonomous.Events.AutoEventCollectSteak;
import frc.robot.Autonomous.Events.AutoEventShootFromCollectSteak;
import frc.robot.Autonomous.Events.AutoEventStopRobot;
import frc.robot.Autonomous.Events.AutoEventDriveToBallThief;
import frc.robot.Autonomous.Events.AutoEventPathPlanTest;
import frc.robot.Autonomous.Events.AutoEventTurn;
import frc.robot.Autonomous.Events.AutoEventTurnToVisionTarget;
import frc.robot.Autonomous.Events.AutoEventWait;
import frc.robot.Drivetrain.Drivetrain;
import frc.robot.HumanInterface.DriverController;
import frc.robot.HumanInterface.OperatorController;
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
        BallThief(4),
        Steak(5),
        VisionAlignOnly(6),
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

    boolean driverVisionAlignButtonReleased = false;


    public static final String[] ACTION_MODES =  new String[]{"Do Nothing", 
                                                              "Drive Forward", 
                                                              "Shoot Only", 
                                                              "Vision Align Shoot", 
                                                              "Ball Thief",
                                                              "Steak"};

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
			modeCmd = AutoMode.BallThief;
		} else if (actionStr.compareTo(ACTION_MODES[5]) == 0) { 
			modeCmd = AutoMode.Steak;
		} else { 
			modeCmd = AutoMode.Inactive;
        }

        loadSequencer(true);
    }

    public void sampleOperatorCommands(){
        delayTime_s = 0; //Never delay while operator triggers auto modes

        //TODO - read driver & operator controls, and set the mode command to something meaningful
        modeCmd = AutoMode.Inactive;
        autoModeName = "Inactive";

        boolean tmp = DriverController.getInstance().getAutoHighGoalAlignDesired();
        if(tmp){
            if(driverVisionAlignButtonReleased==true){
                modeCmd = AutoMode.VisionAlignOnly;
                autoModeName = "Driver Commanded Vision Align Only";
                driverVisionAlignButtonReleased = false;
            } else {
                //Do Nothing until driver releases the button
            }
        } else {
            driverVisionAlignButtonReleased = true;
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
                        Drivetrain.getInstance().setInitialPose(11, 10, 90.0);
                    break;
                    case Steak:
                        Drivetrain.getInstance().setInitialPose(11, 10, 90.0);
                    break;
                    default:
                        //Do Nothing
                    break; 
                }
            }

            //Queue up the auto sequence manager with the desired events
            switch(modeCmd){
                case DoNothing:
                    //Empty sequencer - no one here but us chickens.
                break;

                case DriveFwd:
                    //seq.addEvent(new AutoEventDriveForTime(2, 0.25));
                    seq.addEvent(new AutoEventPathPlanTest());
                break;

                case ShootOnly:
                    //TODO
                break;

                case VisionAlignOnly:
                    seq.addEvent(new AutoEventStopRobot());
                    seq.addEvent(new AutoEventWait(0.25));
                    seq.addEvent(new AutoEventTurnToVisionTarget());
                break;

                case VisionAlignShoot:
                    //TODO
                break;

                case BallThief:
                    seq.addEvent(new AutoEventDriveToBallThief());
                    //some event to run intake
                    seq.addEvent(new AutoEventBackUpFromBallThief());
                    seq.addEvent(new AutoEventTurn(180));
                    //some event to shoot balls
                break;

                case Steak:
                    seq.addEvent(new AutoEventDriveToBallThief());
                    //some event to run intake
                    seq.addEvent(new AutoEventBackUpFromBallThief());
                    //some event to shoot balls
                    seq.addEvent(new AutoEventCollectSteak());
                    seq.addEvent(new AutoEventShootFromCollectSteak());
                    //shoot balls from 3 point spot
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