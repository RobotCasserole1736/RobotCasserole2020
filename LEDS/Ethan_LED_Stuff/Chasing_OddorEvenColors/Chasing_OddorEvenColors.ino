/* 
  LED Chasing with a better delay code (doesn't lag) 
  Color of Chaser is random, changes everytime current chaser is gone (Modulos remainder = 0)
*/
#include <FastLED.h>

#define NUM_LEDS 300
#define LED_PIN 2
int val = 4;
int curVal_1;
int resetVal_1;
int odd_even;
int chaserSize=100;
int brightness=20;

CRGB color0=CRGB(random(255), random(255), random(255)); //what color are the LEDs when color1 is not passing through
CRGB color1;


CRGB led[NUM_LEDS];

// THIS IS THE DELAY
const long interval = 10;           // interval at which to blink (milliseconds)


// Generally, you should use "unsigned long" for variables that hold time
// The value will quickly become too large for an int to store
unsigned long previousMillis = 0;        // will store last time LED was updated

void setup() {
  // set the digital pin as output:
  FastLED.addLeds<NEOPIXEL, LED_PIN>(led, NUM_LEDS);
  LEDS.setBrightness(brightness);
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

      val++;
      
//**************************************************************
// Pattern: Random Color Chaser
//**************************************************************
      curVal_1=(val+chaserSize) % NUM_LEDS; //Modulo
      resetVal_1=(val) % NUM_LEDS; //The difference between curVal and resetVal is the length of your chaser
      odd_even = val % 2; // If remainder is 0, even number
      
      if(odd_even == 0){
        color1=CRGB(255, 0, 0); //If LED is even, turn red
      } else {
        color1=CRGB(255, 255, 255); //If LED is odd, turn white
      }


      // If LED is off, turn on, then go to next LED
      if (led[curVal_1] == color0) {
        led[curVal_1] = color1;
      }

      // take previous ON LED, turn it off (=
      if (led[resetVal_1] == color1) {
        led[resetVal_1] = color0;  
      } else {
        led[resetVal_1] = color0;
      }
  FastLED.show();
  }
}
