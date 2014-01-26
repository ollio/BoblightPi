package org.bozan.boblight.output;


import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.bozan.boblight.configuration.BoblightConfiguration;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static org.apache.commons.lang.math.NumberUtils.toInt;

public class IODeviceSerialJSSC extends IODeviceAbstract {

  private final static Logger LOG = Logger.getLogger(IODeviceSerialJSSC.class.getName());

  private SerialPort port;

  public IODeviceSerialJSSC(BoblightConfiguration configuration) {
    super(configuration);
  }

  @Override
  void connect() throws IOException {
    String portName = configuration.getDevice().get("output");
    int rate = toInt(configuration.getDevice().get("rate"));
    LOG.info("Connecting to device: " + portName + " with " + rate + " baud");

    try {
      port = new SerialPort(portName);
      if (port.openPort() && port.setParams(rate, 8, 1, 0)) {
        LOG.info("Port opened success");
      }

      port.addEventListener(new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
          switch(serialPortEvent.getEventType()) {
            case SerialPortEvent.RXCHAR: LOG.info("EVENT RXCHAR"); break;
            case SerialPortEvent.RXFLAG: LOG.info("EVENT RXFLAG"); break;
            case SerialPortEvent.TXEMPTY: LOG.info("EVENT TXEMPTY");
//              serialSender.canSend(true);
              break;
            case SerialPortEvent.CTS: LOG.info("EVENT CTS"); break;
            case SerialPortEvent.DSR: LOG.info("EVENT DSR"); break;
            case SerialPortEvent.RLSD: LOG.info("EVENT RLSD"); break;
            case SerialPortEvent.BREAK: LOG.info("EVENT BREAK"); break;
            case SerialPortEvent.ERR: LOG.info("EVENT ERR"); break;
            case SerialPortEvent.RING: LOG.info("EVENT RING"); break;
          }
          //        LOG.info("Serial Event: " + serialPortEvent.getEventType());
          try {
            String line = port.readString();
            if (line != null) {
              LOG.info(">> " + line);
            }
            sleep(100);
          } catch (Exception e) {
          }
        }
      });
    } catch (SerialPortException e) {
      throw new IOException("Can't open " + portName + ": " + e.getMessage());
    }
    try {
      sleep(1000);
    } catch (InterruptedException e) {
    }
  }

  @Override
  void disconnect() throws IOException {
    try {
      port.closePort();
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  protected synchronized void writeBytes(byte[] data) throws Exception {
    for (byte b : data) {
      port.writeByte(b);
      sleep(2);
    }
  }
}
