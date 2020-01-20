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
    double voltage = 0;

    public ImaginaryShooterControl() {

    }

    public void update() {

        if(run == true){
            voltage=12;
        }else if(run == false){
            voltage=0;
        }
        speed_rpm = ((((.02) voltage * prevSpeedRPM )));
    }

    public boolean isUnderLoad(){
        //Not really doable in simulation
        return false;
    }

    public double getSpeed_rpm(){
        return speed_rpm;
    }
}
