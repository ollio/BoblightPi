package org.bozan.boblight.output;

import org.bozan.boblight.configuration.BoblightConfiguration;


import java.io.IOException;

import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.lowerCase;

public class IODeviceFactory {

  private IODeviceAbstract device;

  public IODevice getIODevice() throws IOException {
    if(device != null) {
      return device;
    }

    BoblightConfiguration config = BoblightConfiguration.getInstance();

    switch (defaultString(lowerCase(config.getDevice().get("type")))) {
      case "serial" :
        device = new IODeviceSerialJSSC(config);
        break;
      case "i2c" :
        device = new IODeviceI2C(config);
        break;
      case "console" :
      case "log" :
        device = new IODeviceLogger(config);
        break;
      default:
        throw new IOException("No such device: " + config.getDevice().get("type"));
    }
    device.connect();
    return device;
  }

}
