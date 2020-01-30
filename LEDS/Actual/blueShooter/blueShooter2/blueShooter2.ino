#include <FastLED.h>
#define NUM_LEDS 200
#define LED_PIN 2
CRGB led[NUM_LEDS];

void setup(){

LEDS.setBrightness(75);
FastLED.addLeds<NEOPIXEL, LED_PIN>(led, NUM_LEDS);
for (int i = 0; i < NUM_LEDS; i++) {
  led[i] = CRGB(0, 0, 255);
}
}

void blip(int x){
  for(int i =0; i<10; i++){
    led[(x+i)%NUM_LEDS] = CRGB::White;
  }
 
}
int counter=0;

void loop(){
  int offset=30;
  int whiteLed=counter%NUM_LEDS;
  blip(whiteLed);
  FastLED.show();
  if(whiteLed+offset<NUM_LEDS){
    blip(whiteLed+offset);
    FastLED.show();
    led[whiteLed+offset] = CRGB(0, 0, 255);
  }else{
    blip(whiteLed-NUM_LEDS+offset);
    FastLED.show();
    led[whiteLed-NUM_LEDS+offset] = CRGB(0, 0, 255);
   }
  delay(10);
  led[whiteLed] = CRGB(0, 0, 255);
  counter++;
}
