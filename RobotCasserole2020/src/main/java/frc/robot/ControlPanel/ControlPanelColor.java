package frc.robot.ControlPanel;


public enum ControlPanelColor{
    kRED(0), kGREEN(1), kBLUE(2), kYELLOW(3), kUNKNOWN(-1);

    public final int value;

    private ControlPanelColor(int value) {
        this.value = value;
    }

}