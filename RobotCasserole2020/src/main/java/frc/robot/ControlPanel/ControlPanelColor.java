package frc.robot.ControlPanel;


public enum ControlPanelColor{
    kRED(0), kGREEN(1), kBLUE(2), kYELLOW(3), kUNKNOWN(-1);

    public final int value;

    private ControlPanelColor(int value) {
        this.value = value;
    }

    public static ControlPanelColor getColorFromInt(int colorValue){

        //wraps colorValue to be positive, -1 equals 3, -2 equals 2 and so on
        //(but giving a value less than -4 will throw an error)
        //which is to then be subject to the modulo operator below the if statement
        if(colorValue >= -4 && colorValue < 0){
            colorValue += 4;
        }

        //wraps integers greater than 3 back into the list
        int wrappedIndex = colorValue % 4;
        if(wrappedIndex == 0){
            return ControlPanelColor.kRED;
        }else if(wrappedIndex == 1){
            return ControlPanelColor.kGREEN;
        }else if(wrappedIndex == 2){
            return ControlPanelColor.kBLUE;
        }else if(wrappedIndex == 3){
            return ControlPanelColor.kYELLOW;
        }else{
            return ControlPanelColor.kUNKNOWN;
        }
    }
}