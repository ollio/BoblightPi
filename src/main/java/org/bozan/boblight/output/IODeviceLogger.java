package org.bozan.boblight.output;


import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

import static java.lang.String.format;

@Component
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

  @Override
  protected void send(byte[] message) {
    LOG.info("<< SEND " + toHexString(message) + " <<");
  }

  private String toHexString(byte[] message) {
    StringBuffer buf = new StringBuffer(message.length);
    for (byte b : message) {
      buf.append(format("%02X ", b));
    }
    return buf.toString();
  }
}
