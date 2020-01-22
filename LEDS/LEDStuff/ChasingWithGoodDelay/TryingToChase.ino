/* 
  LED Chasing with a better delay code (doesn't lag) 
  Current Error - The val integer once it hits NUM_LEDS does not get set back to zero
*/
#include <FastLED.h>

#define NUM_LEDS 100
#define LED_PIN 2
int val = 0;

CRGB led[NUM_LEDS];

// Generally, you should use "unsigned long" for variables that hold time
// The value will quickly become too large for an int to store
unsigned long previousMillis = 0;        // will store last time LED was updated

// THIS IS THE DELAY
const long interval = 10;           // interval at which to blink (milliseconds)

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
    
    // if the LED is off turn it on and vice-versa:


// Maybe instead have a function in here simply to increase #LED, actual coloring is done outside of currentMillis if statement


      // If LED is off, turn on, then go to next LED
      if (led[val] == CRGB(0, 0, 0)) {
        led[val] = CRGB(val, val*2, val+5);
        val++;
      } else if (val == NUM_LEDS){
        val = 0;
      }

      // take previous LED, turn it off 
      if (led[val-2] == CRGB(val, val*2, val+5)) {
        led[val-2] = CRGB(0, 0, 0);  
      } else {
        led[val-2] = CRGB(0, 0, 0);
      }
  FastLED.show();
    
  }
}
