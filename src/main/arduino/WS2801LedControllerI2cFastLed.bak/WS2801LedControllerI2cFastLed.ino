#include "Wire.h"
#include "FastLED.h"

#define I2C_SLAVE_ADDRESS 0x41
#define NUM_LEDS 62
#define SPI_DATA_PIN 10   // Yellow wire on Adafruit Pixels
#define SPI_CLOCK_PIN 11  // Green wire on Adafruit Pixels
#define MAX_MESSAGE_SIZE 512
#define ACTIVITY_LED 13

CRGB leds[NUM_LEDS];

struct DataEvent {
  boolean  ready;
  uint8_t  bufNum;
};

DataEvent dataEvent;

uint8_t dataBuf[2][MAX_MESSAGE_SIZE];
uint8_t bufSelector = 0;
uint16_t writePtr = 0;

void setup() {
  // init LED
  pinMode(ACTIVITY_LED, OUTPUT);

  // initialize serial for output
  Serial.begin(9600);
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
  if(dataEvent.ready) {
    dataEvent.ready = false;
    byte ledCount = dataBuf[dataEvent.bufNum][1];
    digitalWrite(ACTIVITY_LED, dataEvent.bufNum);

    Serial.print("leds: ");
    Serial.print(ledCount);
/*
    Serial.print(" bufNum: ");
    Serial.println(dataEvent.bufNum);
*/

    for(int i=2; i < ledCount * 4; ) {
        byte id = dataBuf[dataEvent.bufNum][i++];
//        leds[id].r = dataBuf[dataEvent.bufNum][i++];
//        leds[id].g = dataBuf[dataEvent.bufNum][i++];
//        leds[id].b = dataBuf[dataEvent.bufNum][i++];
        memcpy(leds[id], dataBuf[dataEvent.bufNum][i], 3);
        i+=3;
    }
    LEDS.show();
    memset(dataBuf[dataEvent.bufNum], 0, NUM_LEDS);
  }
  delay(20);
}

// 'N' CC ID RR GG BB
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

