/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.ShooterControl;

import frc.lib.Calibration.Calibration;

/**
 * Add your docs here.
 */
public class ImaginaryShooterControl extends ShooterControl {

    double speed_rpm = 0;

    public ImaginaryShooterControl() {
        shooterRPMSetpointFar  = new Calibration("Shooter Far Shot Setpoint RPM", 2000);
        shooterRPMSetpointClose= new Calibration("Shooter Close Shot Setpoint RPM", 1500);
    }

    public void update() {
        if(run == ShooterRunCommand.ShotClose){

        }
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
        return ShooterCtrlMode.Stop;
    }
}
