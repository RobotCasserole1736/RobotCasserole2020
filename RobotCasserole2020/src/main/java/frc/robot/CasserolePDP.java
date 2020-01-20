package frc.robot;

import edu.wpi.first.wpilibj.PowerDistributionPanel;

public class CasserolePDP{
    private static PowerDistributionPanel casserolePDP = null;
    public static synchronized PowerDistributionPanel getInstance() {
        if (casserolePDP == null)
            casserolePDP = new PowerDistributionPanel(RobotConstants.POWER_DISTRIBUTION_PANEL_CANID);
        return casserolePDP;
    }
}