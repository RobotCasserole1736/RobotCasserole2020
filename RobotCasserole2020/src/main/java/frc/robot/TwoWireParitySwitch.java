package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import frc.lib.Signal.Annotations.Signal;

public class TwoWireParitySwitch{

    public enum SwitchState {
        Pressed(1),
        NotPressed(0),
        Broken(-1);

        public final int value;

        private SwitchState(int value) {
            this.value = value;
        }
    }

    final int DEBOUNCE_DUR_LOOPS = 5;

    int debounceCounter = DEBOUNCE_DUR_LOOPS;
    DigitalInput no_input;
    DigitalInput nc_input;

    SwitchState stateRaw = SwitchState.NotPressed;
    SwitchState stateRawPrev = SwitchState.NotPressed;
    @Signal
    SwitchState stateDbnc = SwitchState.NotPressed;

    TwoWireParitySwitch(int no_channel, int nc_channel){
        no_input = new DigitalInput(no_channel);
        nc_input = new DigitalInput(nc_channel);
        debounceCounter = DEBOUNCE_DUR_LOOPS;
    }

    public SwitchState get(){
        boolean no_val = no_input.get();
        boolean nc_val = nc_input.get();

        if(no_val == true && nc_val == false){
            stateRaw = SwitchState.NotPressed;
        } else if (no_val == false && nc_val == true){
            stateRaw = SwitchState.Pressed;
        } else {
            stateRaw = SwitchState.Broken;
        }

        if(stateRaw != stateRawPrev){
            debounceCounter = DEBOUNCE_DUR_LOOPS;
        } else {
            if(debounceCounter > 0){
                debounceCounter--;
            }
        }

        if(debounceCounter == 0){
            stateDbnc = stateRaw;
        }

        stateRawPrev = stateRaw;
        return stateDbnc;
    }


}