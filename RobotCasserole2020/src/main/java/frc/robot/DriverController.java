/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;
import edu.wpi.first.wpilibj.XboxController;

/**
 * Add your docs here.
 */
public class DriverController {
    double driverFwdRevCmd;
    double driverRotCmd;

    private static DriverController singularInstance = null;
    public static synchronized DriverController getInstance() {
        if ( singularInstance == null)
            singularInstance = new DriverController();
        return singularInstance;
    }

    driverFwdRevCmd = Utils.ctrlAxisScale(-1*frCmd,  joystickExpScaleFactor.get(), joystickDeadzone.get());
    driverRotateCmd = Utils.ctrlAxisScale(   rCmd, joystickExpScaleFactor.get(), joystickDeadzone.get());


}
