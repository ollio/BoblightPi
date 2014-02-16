#include "Wire.h"
#include "FastLED.h"

// SPI - LED Strip
#define NUM_LEDS 150
#define DATA_PIN 7

// I2C Bus
#define I2C_SLAVE_ADDRESS 0x41

#define MAX_MESSAGE_SIZE 256
#define ACTIVITY_LED 13

CRGB leds[NUM_LEDS];
uint16_t bufPos = 0;
uint16_t bufLen = 0;
uint8_t curLed = 0;

void setup() {
  // init LED
  pinMode(ACTIVITY_LED, OUTPUT);

  // initialize i2c as slave
  Wire.begin(I2C_SLAVE_ADDRESS);
  Wire.onReceive(receiveData);

  // initialize the LED strip
  FastLED.addLeds<WS2812B, DATA_PIN, GRB>(leds, NUM_LEDS);
  clearStrip();
}

void loop() {
  delay(500);
  digitalWrite(ACTIVITY_LED, HIGH);
  delay(500);
  digitalWrite(ACTIVITY_LED, LOW);
}

// 'N' CC ID RR GG BB
// 'N' = Numeric message, C = count of led blocks, ID = LedId (0-FF), RRGGBB = RGB 00-FF
void receiveData(int byteCount) {
  while( Wire.available() ) {
     uint16_t by = Wire.read();
     bufPos++;

     if(bufPos == 1 && by != 'N') {
       bufPos = 0;
     } else if(bufPos == 2) {
       bufLen = by * 4 + 2;
       if(bufLen > MAX_MESSAGE_SIZE) {
         bufPos = 0;
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

       if(bufPos == bufLen) {
         LEDS.show();
         bufPos = 0;
       } else if(bufPos > MAX_MESSAGE_SIZE) {
         bufPos = 0;
       }
     }
  }
}

void clearStrip() {
  int i;
  for (i=0; i < NUM_LEDS; i++) {
    leds[i] = CRGB::Black;
  }
  FastLED.show();
}