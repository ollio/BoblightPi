package org.bozan.boblight.output;


import org.bozan.boblight.configuration.BoblightConfiguration;

import java.io.IOException;
import java.util.logging.Logger;

import static java.lang.String.format;

public class IODeviceLogger extends IODeviceAbstract {
  private final static Logger LOG = Logger.getLogger(IODeviceLogger.class.getName());

  public IODeviceLogger(BoblightConfiguration configuration) {
    super(configuration);
  }

  @Override
  void connect() throws IOException {
     LOG.info("<< CONNECT >>");
  }

  @Override
  protected void writeBytes(byte[] array) throws Exception {
    logData(array);
  }
}
