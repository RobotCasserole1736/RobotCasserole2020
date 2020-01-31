#include <FastLED.h>

#define NUM_LEDS 60
#define LED_PIN 2

CRGB led[NUM_LEDS];

int pin = 9;
unsigned long duration;

void setup() {
  
  LEDS.setBrightness(90);
  FastLED.addLeds<NEOPIXEL, LED_PIN>(led, NUM_LEDS);
  Serial.begin(9600);
  pinMode(pin, INPUT);
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

void setPurple(int val){
  for (int i = 0; i < NUM_LEDS; i++){
    led[i] = CRGB(val, 0, val);
  }
  FastLED.show();
}

void loop() {

  duration = pulseIn(pin, HIGH);
  Serial.println(duration);
  Serial.print(duration);

  if (duration != 0){
 for (int i = 0; i < 196; i++){
  setRed(i);
  delay(10);
 }
 
 for (int i = 195; i > 0; i--){
  setRed(i);
  delay(10);
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
else{
for (int i = 0; i < 256; i++){
  setPurple(i);
  delay(10);
 }
 
 for (int i = 255; i > 0; i--){
  setPurple(i);
  delay(10);
 }
}
}
