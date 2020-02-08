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
int r;
int g;
int b;
int chaserSize=2;

CRGB color0=CRGB(0, 0, 0);
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
  LEDS.setBrightness(100);
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

      if(curVal_1 == 0){
        r=random(255);
        g=random(255);
        b=random(255);
      }

      
      color1=CRGB(r, g, b);

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
