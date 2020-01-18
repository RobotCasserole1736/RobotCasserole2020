#include <FastLED.h>

#define NUM_LEDS 20
#define LED_PIN 2
#define delayVal 1
CRGB led[NUM_LEDS];

void setup() {
  
FastLED.addLeds<NEOPIXEL, LED_PIN>(led, NUM_LEDS);
LEDS.setBrightness(60);
}

void chaseOff(int val){
  led[val] = CRGB(0, 0, 0);
  //led[val] = CRGB(val*2, val, val-6);
  FastLED.show();
}

void chaseOn(int val){
  led[val] = CRGB(val, val*2, val+5);
  FastLED.show();
}

void loop(){
  for (int i = 0; i < NUM_LEDS; i++){
    chaseOn(i);
    delay(delayVal);
    
  for (int i = NUM_LEDS; i > 0; i--){ //195
   chaseOff(i);
   delay(delayVal);
  }
  }
}

// for (int i = NUM_LEDS - 1; i > 0; i--){ //195
//  chaseLED(i);
//  delay(10);
// }
/*
void setRed(int val) {
  for (int i = 0; i < NUM_LEDS; i++) {
    led[val] = CRGB(val, 0, 0);
  }
  for (int i = 0; i == val; i--){
    led[i] = CRGB(0, 0, 0);
  }
 FastLED.show();
}

void loop() {
 for (int i = 0; i < NUM_LEDS; i++){ //196
  setRed(i);
  delay(10);
 }
 
 for (int i = NUM_LEDS - 1; i > 0; i--){ //195
  setRed(i);
  delay(10);
 }

void setWhite(int val){
  for (int i = 0; i < NUM_LEDS; i++){
    led[i] = CRGB(val, val, val);
  }
  FastLED.show();
}


for (int i = 0; i < 256; i++){
  setWhite(i);
  delay(10);
 }
 
 for (int i = 255; i > 0; i--){
  setWhite(i);
  delay(10);
 }
}
*/
// Range finder and laser pointer to calculate center of target
