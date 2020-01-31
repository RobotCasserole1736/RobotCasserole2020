//this code was written on 1/25/20 to test changing color patterns on the arduino based on wether the robot was disabled or not
#include <FastLED.h>
#define NUM_LEDS 60
#define LED_PIN 2
CRGB led[NUM_LEDS];
int pin = 9;
unsigned long duration;

//More technical mumbo-jumbo
void setup() {
  LEDS.setBrightness(90);
  FastLED.addLeds<NEOPIXEL, LED_PIN>(led, NUM_LEDS);
  Serial.begin(9600);
  pinMode(pin, INPUT);
}

void loop() {
  duration = pulseIn(pin, HIGH);
  Serial.println(duration);
  Serial.print(duration);

  //This says if the robot is disabled (pulse=0) run the panelYellow.ino protocol
  if (0 <= duration && duration < 499){
  for (int i = 0; i < NUM_LEDS; i++) {
  led[i] = CRGB(255, 255, 0);
}
FastLED.show();
}

//this says if the pulse is greater than 500, but less than 1200 run the teleopRed.ino protocol
  if (duration > 500 && duration < 1200){
    for (int i = 0; i < NUM_LEDS; i++) {
  led[i] = CRGB(255, 0, 0);
}
FastLED.show();
  }
  
//This says if the pulse greater than 1201 run the teleopBlue.ino protocol
  if (duration > 1201){
    for (int i = 0; i < NUM_LEDS; i++) {
  led[i] = CRGB(0, 0, 255);
}
FastLED.show();
  }
}
