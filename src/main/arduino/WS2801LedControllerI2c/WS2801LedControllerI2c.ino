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

struct DataEvent {
  boolean  ready;
  uint8_t  bufNum;
};

Adafruit_WS2801 strip = Adafruit_WS2801(NUM_LEDS, SPI_DATA_PIN, SPI_CLOCK_PIN);

DataEvent dataEvent;

uint8_t dataBuf[2][MAX_MESSAGE_SIZE];
uint8_t bufSelector = 0;
uint16_t writePtr = 0;


void setup() {
//  Serial.begin(BAUD_RATE);

  // init LED
  pinMode(ACTIVITY_LED, OUTPUT);

  // initialize i2c as slave
  Wire.begin(I2C_SLAVE_ADDRESS);
  Wire.onReceive(receiveData);

  // initialize the LED strip
  strip.begin();
  blinkStripRGB();

//  Serial.println("Init complete");
}

void loop() {
  if(dataEvent.ready) {
    dataEvent.ready = false;
    int leds = dataBuf[dataEvent.bufNum][1];
    digitalWrite(ACTIVITY_LED, dataEvent.bufNum);

/*
    Serial.print("leds: ");
    Serial.print(leds);
    Serial.print(" bufNum: ");
    Serial.println(dataEvent.bufNum);
*/

    for(int i=2; i < leds * 4; ) {
        uint16_t led = dataBuf[dataEvent.bufNum][i++];
        strip.setPixelColor(led, color(dataBuf[dataEvent.bufNum][i++],      // R
                                       dataBuf[dataEvent.bufNum][i++],      // G
                                       dataBuf[dataEvent.bufNum][i++]));     // B
    }
    strip.show();
    delay(20);
    memset(dataBuf[dataEvent.bufNum], 0, leds * 4);
  } else {
    delay(20);
  }
}

// 'N' LL ID RR GG BB
// 'N' = Numeric message, C = count of led blocks, ID = LedId (0-FF), RRGGBB = RGB 00-FF
void receiveData(int byteCount) {
  while( Wire.available() ) {
    dataBuf[bufSelector][writePtr++] = Wire.read();
    if(writePtr == 1) {
      if(dataBuf[bufSelector][0] != 'N') {
         writePtr = 0;
      }
    } else if(writePtr >= 2 &&
              writePtr >= (dataBuf[bufSelector][1] * 4 + 2)) {
      dataEvent.ready = true;
      dataEvent.bufNum = bufSelector;
      bufSelector = (bufSelector + 1) % 2;
      writePtr = 0;
    }
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
  c = r;
  c <<= 8;
  c |= g;
  c <<= 8;
  c |= b;
  return c;
}

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
