package org.bozan.boblight.output;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.bozan.boblight.configuration.BoblightConfiguration;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static org.apache.commons.lang.math.NumberUtils.toInt;

public class IODeviceI2C extends IODeviceAbstract {
  private final static Logger LOG = Logger.getLogger(IODeviceI2C.class.getName());

  private I2CBus i2CBus;
  private int deviceId;
  private I2CDevice i2CDevice;

  public IODeviceI2C(BoblightConfiguration configuration) {
    super(configuration);
  }

  @Override
  void connect() throws IOException {
    LOG.info("Initialize I2C Bus: " + I2CBus.BUS_1);
    i2CBus = I2CFactory.getInstance(I2CBus.BUS_1);
    deviceId = Integer.parseInt(configuration.getDevice().get("i2cDevice"), 16);
    LOG.info(format("Fetching device ID: 0x%02X ", deviceId));
    i2CDevice = i2CBus.getDevice(deviceId);
  }

  @Override
  protected synchronized void writeBytes(byte[] data) throws Exception {
    for (byte b : data) {
      i2CDevice.write(b);
//      sleep(1);
    }
  }
}
