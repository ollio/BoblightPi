package org.bozan.boblight.output;


import java.io.IOException;
import java.util.logging.Logger;

import static java.lang.String.format;

public class IODeviceLogger extends IODeviceAbstract {
  private final static Logger LOG = Logger.getLogger(IODeviceLogger.class.getName());


  @Override
  void connect() throws IOException {
     LOG.info("<< CONNECT >>");
  }

  @Override
  public void setLight(int ledId, int r, int g, int b) {
    LOG.info(format(">> LED %d RGB: #%2X%2X%2X >>", ledId, r, g, b));
    super.setLight(ledId, r, g, b);
  }
}
