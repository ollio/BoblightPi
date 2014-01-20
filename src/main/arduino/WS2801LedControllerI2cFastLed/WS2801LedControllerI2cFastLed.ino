#include "Wire.h"
#include "FastLED.h"

#define I2C_SLAVE_ADDRESS 0x41
#define NUM_LEDS 62
#define SPI_DATA_PIN 10   // Yellow wire on Adafruit Pixels
#define SPI_CLOCK_PIN 11  // Green wire on Adafruit Pixels
#define MAX_MESSAGE_SIZE 512
#define ACTIVITY_LED 13

CRGB leds[NUM_LEDS];
boolean  dataReady;
uint16_t bufPos = 0;
uint16_t bufLen = 0;
uint8_t curLed = 0;

void setup() {
  // init LED
  pinMode(ACTIVITY_LED, OUTPUT);

  // initialize serial for output
//  Serial.begin(9600);
//    while (!Serial) {
//      ; // wait for serial port to connect.
//    }

//  Serial.print("Number of leds: ");
//  Serial.println(NUM_LEDS);
//  Serial.println("Valid command: T00FF00000100FF00020000FF");

  // initialize i2c as slave
  Wire.begin(I2C_SLAVE_ADDRESS);
  Wire.onReceive(receiveData);
//  Wire.onRequest(sendData);

  // initialize the LED strip
  FastLED.addLeds<WS2801, SPI_DATA_PIN, SPI_CLOCK_PIN, BGR>(leds, NUM_LEDS);
  blinkStripRGB();
}

void loop() {
  if(dataReady) {
    dataReady = false;
    
//    Serial.println("UPDATE");

    LEDS.show();
  }
  delay(20);
}

// 'N' CC ID RR GG BB
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
        leds[curLed].r = by;
      } else if(bufPos % 4 == 0) {
        leds[curLed].g = by;
      } else if(bufPos % 4 == 1) {
        leds[curLed].b = by;
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

void clearStrip() {
  int i;
  for (i=0; i < NUM_LEDS; i++) {
    leds[i] = CRGB::Black;
  }
  FastLED.show();
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

