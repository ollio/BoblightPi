package org.bozan.boblight.output;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import jssc.SerialPortException;
import org.apache.commons.lang.math.NumberUtils;
import org.bozan.boblight.configuration.BoblightConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static org.apache.commons.lang.math.NumberUtils.toDouble;
import static org.apache.commons.lang.math.NumberUtils.toInt;

@Component
@EnableScheduling
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
    LOG.info(format("Fetching device ID: 0x%02X ", deviceId));
    device = bus.getDevice(deviceId);
  }

  @Scheduled(initialDelay = 1000, fixedRate = 50)
  public void reportCurrentTime() {
    while (!messageQueue.isEmpty()) {
      try {
        int blocks = messageQueue.size();
        blocks = Math.min(blocks, configuration.getMaxBlocks());

        ByteBuffer buf = ByteBuffer.allocate(blocks * 4 + 2);
        buf.put((byte) 'N');
        buf.put((byte) blocks);
        for (int i = 0; i < blocks; i++) {
          buf.put(messageQueue.poll());
        }

        logData(buf.array());
        device.write(buf.array(), 0, buf.array().length);
      } catch (IOException e) {
        LOG.log(Level.SEVERE, "Can't send to I2C port: " + e.getMessage(), e);
      }
      try {
        sleep(10);
      } catch (InterruptedException e) {
      }
    }
  }

}
