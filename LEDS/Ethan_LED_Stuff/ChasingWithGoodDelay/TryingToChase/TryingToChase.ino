/* 
  LED Chasing with a better delay code (doesn't lag) 
*/
#include <FastLED.h>

#define NUM_LEDS 60
#define LED_PIN 2
int val = 4;
int curVal_1;
int resetVal_1;
int curVal_2;
int resetVal_2;
CRGB color0=CRGB(0, 0, 0);
CRGB color1;
CRGB color2;

CRGB led[NUM_LEDS];

// Generally, you should use "unsigned long" for variables that hold time
// The value will quickly become too large for an int to store
unsigned long previousMillis = 0;        // will store last time LED was updated

// THIS IS THE DELAY
const long interval = 100;           // interval at which to blink (milliseconds)

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
// Pattern: White Chaser
//**************************************************************
      curVal_1=(val+7) % NUM_LEDS;
      resetVal_1=(val+5) % NUM_LEDS; ////Gap (of 2 MORE if negative and two LESS if positive) than curVal required between cur and reset
      
      color1=CRGB(curVal_1, curVal_1*2, curVal_1+5);


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

//**************************************************************
// Pattern: Blue Chaser
//**************************************************************
      curVal_2=(val-5) % NUM_LEDS;    //Modulo
      resetVal_2=(val-7) % NUM_LEDS;   //The difference between curVal and resetVal is the length of your chaser
      
      color2=CRGB(random(val), random(val), random(val));


      // If LED is off, turn on, then go to next LED
      if (led[curVal_2] == color0) {
        led[curVal_2] = color2;
      }

      // take previous ON LED, turn it off (=
      if (led[resetVal_2] == color2) {
        led[resetVal_2] = color0;  
      } else {
        led[resetVal_2] = color0;
      }
  FastLED.show();
    
  }
}
