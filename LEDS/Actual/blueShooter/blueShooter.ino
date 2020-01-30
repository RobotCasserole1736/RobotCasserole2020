#include <FastLED.h>

#define NUM_LEDS 200
#define LED_PIN 2

CRGB led[NUM_LEDS];

void setup() {
  LEDS.setBrightness(75);
FastLED.addLeds<NEOPIXEL, LED_PIN>(led, NUM_LEDS);

}

void setBlue(int val) {
  for (int i = 0; i < NUM_LEDS; i++) {
    led[i] = CRGB(0, 0, val);
  }
 FastLED.show();
}
void setWhite(int val){
  for (int i = 0; i < NUM_LEDS; i++){
    led[i] = CRGB(val, val, val);
  }
  FastLED.show();
}


void loop() {
 for (int i = 0; i < 196; i++){
  setBlue(i);
 }
 
 for (int i = 195; i > 0; i--){
  setBlue(i);
 }

for (int i = 0; i < 256; i++){
  setWhite(i);
 }
 for (int i = 255; i > 0; i--){
  setWhite(i);
 }
}

/*
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
      */
