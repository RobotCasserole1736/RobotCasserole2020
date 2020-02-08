#include <FastLED.h>
#include "Fire.h"

//Constants related to hardware setup
#define NUM_LEDS 100
#define LED_PIN 2
#define ROBORIO_DATA_PIN 9

// Overall brigness control
#define BRIGHTNESS 70
// Desired FPS for the strip
#define FRAMES_PER_SECOND 120

//Buffer containing the desired color of each LED
CRGB led[NUM_LEDS];
int pulseLen_us;

/**
 * One-time Init, at startup
 */
void setup()
{
  FastLED.setBrightness(BRIGHTNESS);
  FastLED.addLeds<NEOPIXEL, LED_PIN>(led, NUM_LEDS);

  //Set up a debug serial port
  Serial.begin(9600);

  //Configure the roboRIO communication pin to recieve data
  pinMode(ROBORIO_DATA_PIN, INPUT);

  FastLED.show(); //Ensure we get one LED update in prior to periodic - should blank out all LED's
}
/**
 * Periodic call. Will be called again and again.
 */
void loop()
{
  if ((pulseLen_us >= -50) && (pulseLen_us <= 50))
  {
    //Disabled Pattern
    CasseroleColorStripeChase_update();
  }
  else if ((pulseLen_us >= 900) && (pulseLen_us <= 1000))
  {
    //TODO - Call periodic update for pattern 0
    ColorSparkle_update(255, 0, 0);
    //red color sparkle
  }
  else if ((pulseLen_us >= 1200) && (pulseLen_us <= 1299))
  {
    //TODO - Call periodic update for pattern 1
    ColorSparkle_update(0, 0, 255);
    //blue color sparkle
  }
  else if ((pulseLen_us >= 1300) && (pulseLen_us <= 1449))
  {
    //TODO - Call periodic update for pattern 2
    ColorSparkle_update(0, 255, 255);
    //purple color sparkle
  }
  else if ((pulseLen_us >= 1450) && (pulseLen_us <= 1499))
  {
    //TODO - Call periodic update for pattern 3
    ColorSparkle_update(255, 255, 0);
    //yellow color sparkle
  }
  else if ((pulseLen_us >= 1600) && (pulseLen_us <= 1699))
  {
    //TODO - Call periodic update for pattern 4
    blueFade();
  }
  else if ((pulseLen_us >= 1700) && (pulseLen_us <= 1799))
  {
    //TODO - Call periodic update for pattern 5
    redFade();
  }
  else if ((pulseLen_us >= 1901) && (pulseLen_us <= 2000))
  {
    //TODO - Call periodic update for pattern 6
    Rainbow_Fade_Chase();
  }

  // send the 'leds' array out to the actual LED strip
  FastLED.show();
  // insert a delay to keep the framerate modest
  FastLED.delay(1000 / FRAMES_PER_SECOND);

  // do some periodic updates
  EVERY_N_MILLISECONDS(200)
  {
    pulseLen_us = pulseIn(ROBORIO_DATA_PIN, HIGH, 50000);
    Serial.println(pulseLen_us);
  }
}

//**************************************************************
// Pattern: Casserole Color Stripes
//**************************************************************
#define STRIPE_WIDTH_PIXELS 10.0
#define STRIPE_SPEED_PIXELS_PER_LOOP 0.01
void CasseroleColorStripeChase_update()
{
  static double zeroPos = 0;
  zeroPos += STRIPE_SPEED_PIXELS_PER_LOOP;
  for (int i = 0; i < NUM_LEDS; i++)
  {

    //Create a "bumpy" waveform that shifts down the strip over time
    //Output range shoudl be [0,1]
    double pctDownStrip = (double)i / NUM_LEDS;
    double numCyclesOnStrip = (double)NUM_LEDS / (double)STRIPE_WIDTH_PIXELS / 2.0;
    double colorBump = sin(2 * PI * numCyclesOnStrip * (pctDownStrip - zeroPos)) * 0.5 + 0.5;

    //Square the value so that the edge is sharper.
    colorBump *= colorBump;

    //Scale to LED units
    colorBump *= 255;

    //Set the pixel color
    setPixel(i, 255,          //Red
             (int)colorBump,  //Green
             (int)colorBump); //Blue
  }
}

//**************************************************************
// Pattern: Solid Color Sparkle
//**************************************************************
#define CYCLE_FREQ_LOOPS
void ColorSparkle_update(int red, int grn, int blu)
{
  for (int i = 0; i < NUM_LEDS; i++)
  {

    //Set all LED's to the input color, but
    //Randomly set an LED to white.
    if (random(0, NUM_LEDS) <= 1)
    {
      //shiny!
      setPixel(i, 255,    //Red
               (int)255,  //Green
               (int)255); //Blue
    }
    else
    {
      //Normal Color
      setPixel(i, red,    //Red
               (int)grn,  //Green
               (int)blu); //Blue
    }
  }
}
//**************************************************************
// Pattern: Rainbow Fade Chase
//**************************************************************
void Rainbow_Fade_Chase()
{
  /*you must set all LEDS to black so the effect works properly
  for (int i = 0; i < NUM_LEDS; i++)
  {
    led[i] = CRGB(0, 0, 0);
  }*/
  static uint8_t hue = 0;
  //establish a counter
  static int counter = 0;
  static boolean up;
  if (counter == 0)
  {
    up = true;
  }
  else if (counter == NUM_LEDS)
  {
    up = false;
  }
  if (up == true)
  {
    counter++;
  }
  else if (up == false)
  {
    counter--;
  }
  led[counter] = CHSV(hue++, 255, 255);
  fadeall();
  led[counter] = CHSV(hue++, 255, 255);
  fadeall();
}

//**************************************************************
// Pattern:Blue Fade
//**************************************************************
void Blue_Mode_Maker(){
  static int b = 0;
  static int mode = 0;
  b++;
  for (int i = 0; i < NUM_LEDS; i++){
    if (b == 255){
      mode = 1;
    }
    else if (b == 510){
      mode = 0;
    }
    if (b >= 511){
      b = 0;
    }
    if (mode == 1){
      led[i] = CRGB(0, 0, b);
    }
    else if (mode == 0){
      led[i] = CRGB(b, b, b);
    }
  }
}
void blueFade(){
  Blue_Mode_Maker();
  fadeall();
  }
//**************************************************************
// Pattern:Red Fade
//**************************************************************
void Red_Mode_Maker(){
  static int r = 0;
  static int mode = 0;
  r++;
  for (int i = 0; i < NUM_LEDS; i++){
    if (r == 255){
      mode = 1;
    }
    else if (r == 510){
      mode = 0;
    }
    if (r >= 511){
      r = 0;
    }
    if (mode == 1){
      led[i] = CRGB(r, 0, 0);
    }
    else if (mode == 0){
      led[i] = CRGB(r, r, r);
    }
  }
}
void redFade(){
  Red_Mode_Maker();
  fadeall();
  }

//**************************************************************
// Utilities
//**************************************************************
void setPixel(int Pixel, byte red, byte green, byte blue)
{
  // FastLED
  led[Pixel].r = red;
  led[Pixel].g = green;
  led[Pixel].b = blue;
}
void fadeall(){
  for (int i = 0; i < NUM_LEDS; i++){
    led[i].nscale8(250);
  }
}
