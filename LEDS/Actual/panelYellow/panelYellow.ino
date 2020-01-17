#include <FastLED.h>

#define NUM_LEDS 20
#define LED_PIN 2

CRGB led[NUM_LEDS];

void setup() {
LEDS.setBrightness(60);
FastLED.addLeds<NEOPIXEL, LED_PIN>(led, NUM_LEDS);

for (int i = 0; i < NUM_LEDS; i++) {
  led[i] = CRGB(255, 255, 0);

}
FastLED.show();
}
void loop() {

}
