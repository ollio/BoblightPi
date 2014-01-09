package org.bozan.boblight.output;

import org.bozan.boblight.configuration.BoblightConfiguration;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.lowerCase;

@Component
public class IODeviceFactory {

  @Inject
  BoblightConfiguration configuration;

  @Inject
  IODeviceSerialJSSC ioDeviceSerialJSSC;

  @Inject
  IODeviceLogger ioDeviceLogger;

  @Inject
  IODeviceI2C ioDeviceI2C;

  public IODevice getIODevice() throws IOException {
    IODeviceAbstract device;
    switch (defaultString(lowerCase(configuration.getDevice().get("type")))) {
      case "serial" :
        device = ioDeviceSerialJSSC;
        break;
      case "i2c" :
        device = ioDeviceI2C;
        break;
      case "log" :
        device = ioDeviceLogger;
        break;
      default:
        throw new IOException("No such device: " + configuration.getDevice().get("type"));
    }
    device.connect();
    return device;
  }

}
