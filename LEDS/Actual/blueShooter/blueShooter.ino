#include <FastLED.h>

#define NUM_LEDS 10
#define LED_PIN 2

CRGB led[NUM_LEDS];

void setup() {
  
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
  delay(5);
 }
 
 for (int i = 195; i > 0; i--){
  setBlue(i);
  delay(5);
 }

for (int i = 0; i < 256; i++){
  setWhite(i);
  delay(5);
 }
 
 for (int i = 255; i > 0; i--){
  setWhite(i);
  delay(5);
 }
}
