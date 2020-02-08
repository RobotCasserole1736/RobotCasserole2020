/* 
    SIGNIFICANTLY FASTER THAN Delay(int)
    Though still inconsistent, this definitely can handle high LED amounts
*/
#include <FastLED.h>

#define NUM_LEDS 300
#define LED_PIN 2

CRGB led[NUM_LEDS];

// Generally, you should use "unsigned long" for variables that hold time
// The value will quickly become too large for an int to store
unsigned long previousMillis = 0;        // will store last time LED was updated

// constants won't change :
const long interval = 1000;           // interval at which to blink (milliseconds)

void setup() {
  // set the digital pin as output:
  FastLED.addLeds<NEOPIXEL, LED_PIN>(led, NUM_LEDS);
  LEDS.setBrightness(10);
}


void loop() {
 
  // check to see if it's time to blink the LED; that is, if the
  // difference between the current time and last time you blinked
  // the LED is bigger than the interval at which you want to
  // blink the LED.
  unsigned long currentMillis = millis();

  if (currentMillis - previousMillis >= interval) {
    // save the last time you blinked the LED
    previousMillis = currentMillis;
    
  for (int i = 0; i < NUM_LEDS; i++){
    chaseOn(i);
  }
  for (int i = NUM_LEDS; i > 0; i--){
   chaseOff(i);
  }
//    // if the LED is off turn it on and vice-versa:
//    if (led[val] == CRGB(0, 0, 0)) {
//      led[val] = CRGB(val, val*2, val+5);
//    } else {
//      led[val] = CRGB(0, 0, 0);
//    }

    
  FastLED.show();
  }
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
