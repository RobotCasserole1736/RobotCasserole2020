package frc.robot.Autonomous;

import java.util.Set;

import frc.lib.AutoSequencer.AutoSequencer;
import frc.lib.Autonomous.AutoMode;
import frc.lib.Autonomous.AutoModeList;
import frc.lib.Util.CrashTracker;
import frc.lib.miniNT4.LocalClient;
import frc.lib.miniNT4.NT4Server;
import frc.lib.miniNT4.NT4TypeStr;
import frc.lib.miniNT4.samples.TimestampedInteger;
import frc.lib.miniNT4.samples.TimestampedValue;
import frc.lib.miniNT4.topics.Topic;
import frc.robot.Autonomous.Modes.CloseVisionAlignShoot;
import frc.robot.Autonomous.Modes.DoNothing;
import frc.robot.Autonomous.Modes.DriveFwd;
import frc.robot.Autonomous.Modes.ShootOnly;
import frc.robot.Autonomous.Modes.VisionAlignShoot;
import frc.robot.Autonomous.Modes.Wait;
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


public class Autonomous extends LocalClient  {

    Topic curDelayModeTopic = null;
    Topic curMainModeTopic = null;

    int curDelayMode_dashboard = 0;
    int curMainMode_dashboard = 0;

    public AutoModeList mainModeList = new AutoModeList("main");
    public AutoModeList delayModeList = new AutoModeList("delay");

    AutoMode curDelayMode = null;
    AutoMode curMainMode = null;

    AutoMode prevDelayMode = null;
    AutoMode prevMainMode = null;

    
    /* Singleton infratructure*/
    private static Autonomous inst = null;
    public static synchronized Autonomous getInstance() {
        if (inst == null)
            inst = new Autonomous();
        return inst;
    }

    AutoSequencer seq;


    private Autonomous(){
        seq = new AutoSequencer("Autonomous");

        delayModeList.add(new Wait(0.0));
        delayModeList.add(new Wait(3.0));
        delayModeList.add(new Wait(6.0));
        delayModeList.add(new Wait(9.0));

        mainModeList.add(new DoNothing());
        mainModeList.add(new DriveFwd());
        mainModeList.add(new ShootOnly());
        mainModeList.add(new VisionAlignShoot());
        mainModeList.add(new CloseVisionAlignShoot());

        // Create and subscribe to NT4 topics
        curDelayModeTopic = NT4Server.getInstance().publishTopic(delayModeList.getCurModeTopicName(), NT4TypeStr.INT, this);
        curMainModeTopic = NT4Server.getInstance().publishTopic(mainModeList.getCurModeTopicName(), NT4TypeStr.INT, this);
        curDelayModeTopic.submitNewValue(new TimestampedInteger(0, 0));
        curMainModeTopic.submitNewValue(new TimestampedInteger(0, 0));

        this.subscribe(Set.of(delayModeList.getDesModeTopicName(), mainModeList.getDesModeTopicName()), 0).start();

    }

    /* This should be called periodically in Disabled, and once in auto init */
    public void sampleDashboardSelector(){

        curDelayMode = delayModeList.get(curDelayMode_dashboard);
        curMainMode = mainModeList.get(curMainMode_dashboard);	
        loadSequencer(true);
    }


    public void sampleOperatorCommands(){
        curDelayMode = delayModeList.getDefault(); //Never delay while operator triggers auto modes

        if(DriverController.getInstance().getAutoAlignAndShootCmd()){
            curMainMode = mainModeList.get("VisionAlignShoot");
        } else if(DriverController.getInstance().getAutoAlignCmd()){
            //TODO
        } else if(DriverController.getInstance().getAutoAlignAndShootCloseCmd()){
            curMainMode = mainModeList.get("CloseVisionAlignShoot");
        } else {
            curMainMode = null;
        }


        if(curMainMode != prevMainMode){
            //Load/run the command immediately.
            loadSequencer(false);
            startSequencer();
        }

    }


    public void startSequencer(){
        if(curMainMode != null){
            seq.start();
        }
    }

    public void loadSequencer(boolean resetPose){
        
        if(curDelayMode != prevDelayMode || curMainMode != prevMainMode){

            CrashTracker.logGenericMessage("Initing new auto routine " + curDelayMode.humanReadableName + "s delay, " + curMainMode.humanReadableName);

            //Ensure everything on the robot is stopped
            seq.stop();
            Drivetrain.getInstance().setOpenLoopCmd(0, 0);
            ShooterControl.getInstance().setRun(ShooterRunCommand.Stop);
            seq.clearAllEvents();

            // If desired, make sure we set our robot's initial position. This should really only be for
            //   auto, when we have an routine with a known & fixed start location.
            if(resetPose){
                curMainMode.setInitialPose(Drivetrain.getInstance());
            }

            if(curDelayMode != null){
                curDelayMode.addStepsToSequencer(seq);
            }

            if(curMainMode != null){
                curMainMode.addStepsToSequencer(seq);
            }
            
            prevDelayMode = curDelayMode;
            prevMainMode = curMainMode;
        }
    }


    /* This should be called periodically, always */
    public void update(){
        seq.update();
    }

    /* Should be called when returning to disabled to stop everything */
    public void reset(){
        curDelayMode = null;
        curMainMode = null;
        loadSequencer(false);
    }

    public boolean isActive(){
        return (seq.isRunning() && curMainMode != null);
    }

    @Override
    public void onAnnounce(Topic newTopic) {}
    @Override
    public void onUnannounce(Topic deadTopic) {}

    @Override
    public void onValueUpdate(Topic topic, TimestampedValue newVal) {
        if(topic.name.equals(delayModeList.getDesModeTopicName())){
            curDelayMode_dashboard = (Integer) newVal.getVal();
        } else if(topic.name.equals(mainModeList.getDesModeTopicName())){
            curMainMode_dashboard =(Integer) newVal.getVal();
        }         
    }
}