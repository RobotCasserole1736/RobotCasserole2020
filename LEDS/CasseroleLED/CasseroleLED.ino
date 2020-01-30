#include <FastLED.h>

//Constants related to hardware setup
#define NUM_LEDS 80
#define LED_PIN 2
#define ROBORIO_DATA_PIN 9

// Overall brigness control
#define BRIGHTNESS          100
// Desired FPS for the strip
#define FRAMES_PER_SECOND  120

//Buffer containing the desired color of each LED
CRGB led[NUM_LEDS];
int pulseLen_us;

/**
 * One-time Init, at startup
 */
void setup() {
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
    //CasseroleColorStripeChase_update();
    ColorSparkle_update(0, 255, 0);
  }

  if ((pulseLen_us >= 950) && (pulseLen_us <= 1050))
  {
    //TODO - Call periodic update for pattern 0
  }

  if ((pulseLen_us >= 1200) && (pulseLen_us <= 1300))
  {
    //TODO - Call periodic update for pattern 0
  }

  if ((pulseLen_us >= 1450) && (pulseLen_us <= 1550))
  {
    //TODO - Call periodic update for pattern 0
  }

  if ((pulseLen_us >= 1700) && (pulseLen_us <= 1800))
  {
    //TODO - Call periodic update for pattern 0
  }

  if ((pulseLen_us >= 1950) && (pulseLen_us <= 2050))
  {
    //TODO - Call periodic update for pattern 0
  }

  // send the 'leds' array out to the actual LED strip
  FastLED.show();
  // insert a delay to keep the framerate modest
  FastLED.delay(1000 / FRAMES_PER_SECOND);

  // do some periodic updates
  EVERY_N_MILLISECONDS(200) {
    pulseLen_us = pulseIn(ROBORIO_DATA_PIN, HIGH, 50000);
  }
}


//**************************************************************
// Pattern: Casserole Color Stripes
//**************************************************************
#define STRIPE_WIDTH_PIXELS 10.0
#define STRIPE_SPEED_PIXELS_PER_LOOP 0.01
void CasseroleColorStripeChase_update() {
  static double zeroPos = 0;
  zeroPos += STRIPE_SPEED_PIXELS_PER_LOOP;
  for (int i = 0; i < NUM_LEDS; i++) {

    //Create a "bumpy" waveform that shifts down the strip over time
    //Output range shoudl be [0,1]
    double pctDownStrip = (double)i/NUM_LEDS;
    double numCyclesOnStrip = (double)NUM_LEDS / (double)STRIPE_WIDTH_PIXELS / 2.0;
    double colorBump = sin(2*PI*numCyclesOnStrip*(pctDownStrip - zeroPos))*0.5 + 0.5;

    //Square the value so that the edge is sharper.
    colorBump *= colorBump;

    //Scale to LED units
    colorBump *= 255;

    //Set the pixel color
    setPixel(i, 255, //Red
           (int)colorBump, //Green
           (int)colorBump); //Blue
  }
}

//**************************************************************
// Pattern: Solid Color Sparkle
//**************************************************************
#define CYCLE_FREQ_LOOPS
void ColorSparkle_update(int red, int grn, int blu) {
  for (int i = 0; i < NUM_LEDS; i++) {

    //Set all LED's to the input color, but
    //Randomly set an LED to white. 
    if(random(0,NUM_LEDS) <= 1){
      //shiny!
      setPixel(i, 255, //Red
            (int) 255, //Green
            (int) 255); //Blue
    } else {
      //Normal Color
      setPixel(i, red, //Red
            (int) grn, //Green
            (int) blu); //Blue
    }

  }
}



//**************************************************************
// Utilities
//**************************************************************
void setPixel(int Pixel, byte red, byte green, byte blue) {
  // FastLED
  led[Pixel].r = red;
  led[Pixel].g = green;
  led[Pixel].b = blue;
}