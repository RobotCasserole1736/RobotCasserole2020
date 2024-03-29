/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.ShooterControl;

import frc.lib.Calibration.Calibration;
import frc.lib.DataServer.Annotations.Signal;
import frc.robot.LoopTiming;
import frc.robot.RobotConstants;

/**
 * Add your docs here.
 */
public class ImaginaryShooterControl extends ShooterControl {

    @Signal
    double speed_rpm = 0;
    @Signal
    double des_speed_rpm = 0;
    @Signal
    ShooterCtrlMode ctrlMode = ShooterCtrlMode.Stop;
    final double SHOOTER_ACCEL_RPM_PER_SEC = 2000;
    final double SHOOTER_DECEL_RPM_PER_SEC = 1000;
    @Signal
    double adjustedSetpointRPM;

    public ImaginaryShooterControl() {
        shooterRPMSetpointFar  = new Calibration("Shooter Far Shot Setpoint RPM", 2000);
        commonInit();
    }

    public void update() {
        adjustedSetpointRPM = shooterRPMSetpointFar.get() + shotAdjustmentRPM;

        if (runCommand == ShooterRunCommand.ShotFar || runCommand == ShooterRunCommand.ShotClose){
            des_speed_rpm = adjustedSetpointRPM;
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
            ctrlMode = ShooterCtrlMode.Stabilize;
        }

    }

    public boolean isUnderLoad() {
        // Not really doable in simulation
        return false;
    }

    public double getSpeed_rpm() {
        return speed_rpm;
    }

    @Override
    public double getSpeedRPM() {
        return speed_rpm;
    }

    public ShooterCtrlMode getShooterCtrlMode() {
        return ctrlMode;
    }

    @Override
    public void updateGains(final boolean forceChange) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getShotCount() {
        return 42;
    }

    @Override
    public double getAdjustedSetpointRPM() {
        return adjustedSetpointRPM;
    }

}
