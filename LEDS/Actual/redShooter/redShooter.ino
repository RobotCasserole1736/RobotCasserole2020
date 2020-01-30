#include <FastLED.h>

#define NUM_LEDS 200
#define LED_PIN 2

CRGB led[NUM_LEDS];

void setup() {
  LEDS.setBrightness(75);
FastLED.addLeds<NEOPIXEL, LED_PIN>(led, NUM_LEDS);

}

void setRed(int val) {
  for (int i = 0; i < NUM_LEDS; i++) {
    led[i] = CRGB(val, 0, 0);
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
  setRed(i);
  delay(1);
 }
 
 for (int i = 195; i > 0; i--){
  setRed(i);
  delay(1);
 }

for (int i = 0; i < 256; i++){
  setWhite(i);
  delay(1);
 }
 
 for (int i = 255; i > 0; i--){
  setWhite(i);
  delay(1);
 }
}
