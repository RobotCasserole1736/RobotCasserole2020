#include <FastLED.h>

#define NUM_LEDS 200

#define DATA_PIN 2

CRGB led[NUM_LEDS];

void setup() {
	// sanity check delay - allows reprogramming if accidently blowing power w/leds
   	delay(2000);
     
LEDS.setBrightness(50);
FastLED.addLeds<NEOPIXEL, DATA_PIN>(led, NUM_LEDS);
      
      for (int i = 0; i < NUM_LEDS; i++) {
  led[i] = CRGB::White;
}
FastLED.show();
}
void loop() {
   // Move a single white led 
   for(int whiteLed = 0; whiteLed < NUM_LEDS; whiteLed = whiteLed + 1){
      led[whiteLed] = CRGB::Red;
      led[whiteLed+1] = CRGB::Red;
      led[whiteLed+2] = CRGB::Red;
      led[whiteLed+3] = CRGB::Red;
      led[whiteLed+4] = CRGB::Red;
      led[whiteLed+5] = CRGB::Red;
      led[whiteLed+6] = CRGB::Red;
      led[whiteLed+7] = CRGB::Red;
      led[whiteLed+8] = CRGB::Red;
      led[whiteLed+8] = CRGB::Red;
      FastLED.show();
      delay(15);
      led[whiteLed] = CRGB::White;
   }
delay(100);
   
   for(int whiteLed = 0; whiteLed < NUM_LEDS; whiteLed = whiteLed - 1){
      led[whiteLed] = CRGB::Red;
      led[whiteLed+1] = CRGB::Red;
      led[whiteLed+2] = CRGB::Red;
      led[whiteLed+3] = CRGB::Red;
      led[whiteLed+4] = CRGB::Red;
      led[whiteLed+5] = CRGB::Red;
      led[whiteLed+6] = CRGB::Red;
      led[whiteLed+7] = CRGB::Red;
      led[whiteLed+8] = CRGB::Red;
      led[whiteLed+8] = CRGB::Red;
      FastLED.show();
      delay(15);
      led[whiteLed] = CRGB::White;
}
}
