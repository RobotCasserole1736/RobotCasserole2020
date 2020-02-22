/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.ShooterControl;

import frc.lib.Calibration.Calibration;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;

/**
 * Add your docs here.
 */
public class ImaginaryShooterControl extends ShooterControl {

    double speed_rpm = 0;
    double des_speed_rpm = 0;
    ShooterCtrlMode ctrlMode = ShooterCtrlMode.Stop;
    final double SHOOTER_ACCEL_RPM_PER_SEC = 2000;
    final double SHOOTER_DECEL_RPM_PER_SEC = 1000;

    public ImaginaryShooterControl() {
        shooterRPMSetpointFar  = new Calibration("Shooter Far Shot Setpoint RPM", 2000);
        shooterRPMSetpointClose= new Calibration("Shooter Close Shot Setpoint RPM", 1500);
        commonInit();
    }

    public void update() {
        if(runCommand == ShooterRunCommand.ShotClose){
            des_speed_rpm = shooterRPMSetpointClose.get();
        } else if (runCommand == ShooterRunCommand.ShotFar){
            des_speed_rpm = shooterRPMSetpointFar.get();
        } else {
            des_speed_rpm = 0;
        }

        if(speed_rpm < des_speed_rpm){
            speed_rpm += SHOOTER_ACCEL_RPM_PER_SEC*RobotConstants.MAIN_LOOP_Ts;
        } else if(speed_rpm > des_speed_rpm){
            speed_rpm -= SHOOTER_DECEL_RPM_PER_SEC*RobotConstants.MAIN_LOOP_Ts;
        }

        if(Math.abs(des_speed_rpm - speed_rpm) < 100){
            ctrlMode = ShooterCtrlMode.HoldForShot;
        } else if (des_speed_rpm < 100){
            ctrlMode = ShooterCtrlMode.Stop;
        } else {
            ctrlMode = ShooterCtrlMode.SpoolUp;
        }


        double sampleTimeMS = LoopTiming.getInstance().getLoopStartTimeSec() * 1000.0;
        rpmDesiredSig.addSample(sampleTimeMS, des_speed_rpm);
        rpmActualSig.addSample(sampleTimeMS, speed_rpm);
        shooterStateCommandSig.addSample(sampleTimeMS, runCommand.value);
        shooterControlModeSig.addSample(sampleTimeMS, ctrlMode.value); 
    }

    public boolean isUnderLoad(){
        //Not really doable in simulation
        return false;
    }

    public double getSpeed_rpm(){
        return speed_rpm;
    }

    @Override
    public double getSpeedRPM() {
        return speed_rpm;
    }

    public ShooterCtrlMode getShooterCtrlMode(){
        return ctrlMode;
    }

    @Override
    public void updateGains(boolean forceChange) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getShotCount() {
        return 42;
    }

}
