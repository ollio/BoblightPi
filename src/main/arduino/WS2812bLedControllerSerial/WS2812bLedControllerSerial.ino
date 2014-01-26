#include "FastLED.h"

// LED Strip
#define NUM_LEDS 150
#define DATA_PIN 6
#define CLOCK_PIN 13

// Serial
#define BAUD_RATE 115000
#define MAX_MESSAGE_SIZE 512

// Define the array of leds
CRGB leds[NUM_LEDS];

boolean  dataReady;
uint16_t bufPos = 0;
uint16_t bufLen = 0;
uint8_t curLed = 0;


void setup() {
  Serial.begin(BAUD_RATE);
  while (!Serial) {
    ; // wait for serial port to connect.
  }
  FastLED.addLeds<WS2812B, DATA_PIN, GRB>(leds, NUM_LEDS);
  clearStrip();
  delay(1000);
  blinkStripRGB();
}

void loop() {
  checkSerial();
  if(dataReady) {
    dataReady = false;
    LEDS.show();
  } else {
    delay(10);
  }
}

// 'N' CC ID RR GG BB
// 'N' = Numeric message, C = count of led blocks, ID = LedId (0-FF), RRGGBB = RGB 00-FF
void checkSerial() {
  while( Serial.available() ) {
    uint16_t by = Serial.read();
    bufPos++;

    if(bufPos == 1 && by != 'N') {
      bufPos = 0;
    } else if(bufPos == 2) {
      bufLen = by * 4 + 2;
      if(bufLen > MAX_MESSAGE_SIZE) {
        bufPos = 0;
        return;
      }
    } else if(bufPos > 2) {
      if(bufPos % 4 == 3) {
        curLed = by;
      } else if(bufPos % 4 == 0) {
        leds[curLed].r = by;
      } else if(bufPos % 4 == 1) {
        leds[curLed].g = by;
      } else if(bufPos % 4 == 2) {
        leds[curLed].b = by;
      }
    }
    if(bufPos == bufLen) {
      dataReady = true;
      bufPos = 0;
    }
    if(bufPos > MAX_MESSAGE_SIZE) {
      bufPos = 0;
    }
  }
}

void blinkStripRGB() {
  int i;
  for (i=0; i < NUM_LEDS; i++) {
    leds[i] = CRGB::Red;
  }
  FastLED.show();
  delay(500);

  for (i=0; i < NUM_LEDS; i++) {
    leds[i] = CRGB::Green;
  }
  FastLED.show();
  delay(500);

  for (i=0; i < NUM_LEDS; i++) {
    leds[i] = CRGB::Blue;
  }
  FastLED.show();
  delay(500);

  clearStrip();
}

void clearStrip() {
  int i;
  for (i=0; i < NUM_LEDS; i++) {
    leds[i] = CRGB::Black;
  }
  FastLED.show();
}
