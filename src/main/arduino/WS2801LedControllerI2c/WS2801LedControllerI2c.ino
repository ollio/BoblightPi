#include "Wire.h"
#include "SPI.h"
#include "Adafruit_WS2801.h"

#define I2C_SLAVE_ADDRESS 0x41
#define MAX_MESSAGE_SIZE 512
#define NUM_LEDS 62
#define SPI_DATA_PIN 2   // Yellow wire on Adafruit Pixels
#define SPI_CLOCK_PIN 3  // Green wire on Adafruit Pixels
#define ACTIVITY_LED 13

Adafruit_WS2801 strip = Adafruit_WS2801(NUM_LEDS, SPI_DATA_PIN, SPI_CLOCK_PIN);

uint8_t dataBuf[MAX_MESSAGE_SIZE];
uint16_t writePtr = 0;

void setup() {
  // init LED
  pinMode(ACTIVITY_LED, OUTPUT);

  // initialize serial for output
  Serial.begin(9600);
//    while (!Serial) {
//      ; // wait for serial port to connect.
//    }

  Serial.print("Number of leds: ");
  Serial.println(strip.numPixels());
  Serial.println("Valid command: T00FF00000100FF00020000FF");

  // initialize i2c as slave
  Wire.begin(I2C_SLAVE_ADDRESS);
  Wire.onReceive(receiveData);
//  Wire.onRequest(sendData);

  // initialize the LED strip
  strip.begin();
  blinkStripRGB();

  // Update LED contents, to start they are all 'off'
  strip.show();
  digitalWrite(ACTIVITY_LED, HIGH);
}


void loop() {
  delay(500);
  digitalWrite(ACTIVITY_LED, LOW);
  delay(500);
  digitalWrite(ACTIVITY_LED, HIGH);
}

// 'T' 'ID' 'RR GG BB'
// 'T' = Text message, 'ID' = LedId (0-99), 'RRGGBB' = RGB 00-FF
// T01FF0000

// 'N' CC ID RR GG BB 
// 'N' = Numeric message, C = count of led blocks, ID = LedId (0-FF), RRGGBB = RGB 00-FF

void receiveData(int byteCount) {
    while( Wire.available() ) {
    dataBuf[writePtr++] = Wire.read();
    if(messageReceived()) {
      writePtr = 0;
      strip.show();
    } else if(writePtr == MAX_MESSAGE_SIZE) {
      writePtr = 0;
    }
  }
}

boolean messageReceived() {
  if(writePtr >= 1) {
    switch(dataBuf[0]) {
      case 'T':
        return decodeTextMessage();
      case 'N':
        return decodeNumericMessage();
      default: // garbage received
        Serial.println("WARNING: garbage received!");
        printDataBuf();
        writePtr = 0;  
    } 
  }
  return false;
}

boolean decodeNumericMessage() {
  if(writePtr >= 2) {
      int blocks = dataBuf[1];
      int len = blocks * 4;
      if(writePtr == len + 2) {
        printDataBuf();

        for(int i=2; i < len; ) {
          strip.setPixelColor(dataBuf[i++], color(dataBuf[i++], dataBuf[i++], dataBuf[i++]));
        }
        return true;
      }
  }
  return false;
}  

// T00FF00000100FF00020000FF
boolean decodeTextMessage() {
  int len = -1;
  for(int i=0; i < writePtr; i++) {
    if(dataBuf[i] == '\n') {
      len = i;
    }
  }  

  if(len > 1) {
    Serial.print("len: ");
    Serial.println(len);
    printDataBuf();
    for(int i=1; i<len; ) {
      char id[2];
      id[0] = dataBuf[i++];
      id[1] = dataBuf[i++];
  
      byte led = atoi(id);
      uint32_t rgb = (atoi(dataBuf[i++]) << 4) | 
                     (atoi(dataBuf[i++]) << 0) |
                     (atoi(dataBuf[i++]) << 12) |
                     (atoi(dataBuf[i++]) << 8) |
                     (atoi(dataBuf[i++]) << 20) |
                     (atoi(dataBuf[i++]) << 16);
      strip.setPixelColor(led, rgb); 
    } 
    return true;
  }
  return false;
}

uint32_t atoi(byte b) {
  if(b >= '0' && b <= '9') {
    return b - 48;
  }
  if(b >= 'A' && b <= 'F') {
    return b - 55;
  }
  if(b >= 'a' && b <= 'f') {
    return b - 87;
  }
}

void clearStrip() {
  int i;
  for (i=0; i < strip.numPixels(); i++) {
     strip.setPixelColor(i, color(0,0,0)); 
  }
  strip.show();
}

void blinkStripRGB() {
  int i;
  for (i=0; i < strip.numPixels(); i++) {
     strip.setPixelColor(i, color(0xFF,0,0));
  }
  strip.show();
  delay(500);
  for (i=0; i < strip.numPixels(); i++) {
     strip.setPixelColor(i, color(0,0xFF,0));
  }
  strip.show();
  delay(500);
  for (i=0; i < strip.numPixels(); i++) {
     strip.setPixelColor(i, color(0,0,0xFF));
  }
  strip.show();
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

void printDataBuf() {
  int i;
  Serial.print("LEN: ");
  Serial.print(writePtr);
  Serial.print(" DATA: ");
  for (i = 0; i < writePtr; i++) {
    Serial.print(dataBuf[i], HEX);
    Serial.print(" ");
  }
  Serial.println();
}
