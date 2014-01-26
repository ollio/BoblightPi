#include "Wire.h"
#include "SPI.h"
#include "Adafruit_WS2801.h"

#define I2C_SLAVE_ADDRESS 0x41
#define NUM_LEDS 62
#define SPI_DATA_PIN 10   // Yellow wire on Adafruit Pixels
#define SPI_CLOCK_PIN 11  // Green wire on Adafruit Pixels
#define MAX_MESSAGE_SIZE 512
#define ACTIVITY_LED 13

//#define BAUD_RATE 57600

Adafruit_WS2801 strip = Adafruit_WS2801(NUM_LEDS, SPI_DATA_PIN, SPI_CLOCK_PIN);

boolean  dataReady;
uint16_t bufPos = 0;
uint16_t bufLen = 0;
uint8_t curLed = 0;
uint8_t curR = 0;
uint8_t curG = 0;
uint8_t curB = 0;

void setup() {
  // init LED
  pinMode(ACTIVITY_LED, OUTPUT);

  // initialize i2c as slave
  Wire.begin(I2C_SLAVE_ADDRESS);
  Wire.onReceive(receiveData);

  // initialize the LED strip
  strip.begin();
  blinkStripRGB();
}

void loop() {
  if(dataReady) {
    dataReady = false;
    strip.show();
  }
  delay(20);
}

// 'N' LL ID RR GG BB
// 'N' = Numeric message, C = count of led blocks, ID = LedId (0-FF), RRGGBB = RGB 00-FF
void receiveData(int byteCount) {
  while( Wire.available() ) {
    uint16_t by = Wire.read();
    if(bufPos == 0 && by != 'N') {
      return;
    } else if(bufPos == 1) {
      bufLen = by * 4 + 2;
      if(bufLen > MAX_MESSAGE_SIZE) {
        bufPos = 0;
        return;
      }
    } else if(bufPos >= 2) {
      if(bufPos % 4 == 2) {
        curLed = by;
      } else if(bufPos % 4 == 3) {
        curR = by;
      } else if(bufPos % 4 == 0) {
        curG = by;
      } else if(bufPos % 4 == 1) {
        curB = by;
        strip.setPixelColor(curLed, color(curR, curG, curB));
      }
    }
    if(bufPos == bufLen - 1) {
      dataReady = true;
      bufPos = 0;
      return;
    }
    if(bufPos >= MAX_MESSAGE_SIZE) {
      bufPos = 0;
      return;
    }
    bufPos++;
  }
}

void blinkStripRGB() {
  int i;
  for (i=0; i < strip.numPixels(); i++) {
     strip.setPixelColor(i, color(0xAA,0x22,0x44));
  }
  strip.show();
  delay(500);
  for (i=0; i < strip.numPixels(); i++) {
     strip.setPixelColor(i, color(0x33,0xCC,0x99));
  }
  strip.show();
  delay(500);
  for (i=0; i < strip.numPixels(); i++) {
     strip.setPixelColor(i, color(0xAA,0x33,0x11));
  }
  strip.show();
  delay(500);
}

void clearStrip() {
  int i;
  for (i=0; i < strip.numPixels(); i++) {
     strip.setPixelColor(i, color(0,0,0));
  }
  strip.show();
}

// Create a 24 bit color value from R,G,B
uint32_t color(byte r, byte g, byte b) {
  uint32_t c;
  c = b;
  c <<= 8;
  c |= g;
  c <<= 8;
  c |= r;
  return c;
}

//uint32_t color(byte r, byte g, byte b) {
//  uint32_t c;
//  c = r;
//  c <<= 8;
//  c |= g;
//  c <<= 8;
//  c |= b;
//  return c;
//}
//
/*void printDataBuf() {
  int i;
  int len = dataBuf[dataEvent.bufNum][1] * 4;
  Serial.print("LEN: ");
  Serial.print(len);
  Serial.print(" DATA: ");
  for (i = 0; i < len; i++) {
    Serial.print(dataBuf[dataEvent.bufNum][i], HEX);
    Serial.print(" ");
  }
  Serial.println();
}*/
