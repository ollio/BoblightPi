package org.bozan.boblight.output;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.apache.commons.lang.math.NumberUtils;
import org.bozan.boblight.configuration.BoblightConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.apache.commons.lang.math.NumberUtils.toInt;

@Component
public class IODeviceI2C extends IODeviceAbstract {
  private final static Logger LOG = Logger.getLogger(IODeviceI2C.class.getName());

  private I2CDevice device;

  @Autowired
  BoblightConfiguration configuration;

  @Override
  void connect() throws IOException {

    int busId = toInt(configuration.getDevice().get("i2cBus"));
    LOG.info("Initialize I2C Bus: " + busId);
    final I2CBus bus = I2CFactory.getInstance(busId);

    int deviceId = Integer.parseInt(configuration.getDevice().get("i2cDevice"), 16);
    LOG.info(format("Fetching device ID: 0x02X ", deviceId));
    device = bus.getDevice(deviceId);
  }

  @Override
  protected void send(byte[] message) {
    try {
      device.write(message, 0, message.length);
    } catch (IOException e) {
      LOG.severe(e.getMessage());
    }
  }
}
