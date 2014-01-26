package org.bozan.boblight.output;

import com.pi4j.wiringpi.Spi;
import org.bozan.boblight.configuration.BoblightConfiguration;

import java.io.IOException;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class IODeviceSPI extends IODeviceAbstract {
  private final static Logger LOG = Logger.getLogger(IODeviceSPI.class.getName());

  // SPI operations
  private static final byte WRITE_CMD = 0x40;
  private static final byte READ_CMD  = 0x41;

  private static final byte IODIRA = 0x00; // I/O direction A
  private static final byte IODIRB = 0x01; // I/O direction B
  private static final byte IOCON  = 0x0A; // I/O config
  private static final byte GPIOA  = 0x12; // port A
  private static final byte GPIOB  = 0x13; // port B
  private static final byte GPPUA  = 0x0C; // port A pullups
  private static final byte GPPUB  = 0x0D; // port B pullups
  private static final byte OUTPUT_PORT = GPIOA;
  private static final byte INPUT_PORT  = GPIOB;
  private static final byte INPUT_PULLUPS = GPPUB;

  public IODeviceSPI(BoblightConfiguration configuration) {
    super(configuration);
  }

  @Override
  void connect() throws IOException {
    LOG.info("Initialize SPI Master ");

    int fd = Spi.wiringPiSPISetup(0, 10000000);
    if (fd <= -1) {
      throw new IOException("SPI SETUP FAILED");
    }

    // initialize
    write(IOCON,  0x08);  // enable hardware addressing
    write(GPIOA,  0x00);  // set port A off
    write(IODIRA, 0);     // set port A as outputs
    write(IODIRB, 0xFF);  // set port B as inputs
    write(GPPUB,  0xFF);  // set port B pullups on
  }

  synchronized void write(byte register, int data){

    // send test ASCII message
    byte packet[] = new byte[3];
    packet[0] = WRITE_CMD;  // address byte
    packet[1] = register;  // register byte
    packet[2] = (byte)data;  // data byte

    logData(packet);

    Spi.wiringPiSPIDataRW(0, packet, packet.length);
  }

  @Override
  protected synchronized void writeBytes(byte[] data) throws Exception {
    for (byte b : data) {
      write(GPIOA, b);
      sleep(1);
    }
  }
}
