package org.bozan.boblight.output;


import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.bozan.boblight.configuration.BoblightConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Thread.sleep;
import static org.apache.commons.lang.math.NumberUtils.toInt;

@Component
public class IODeviceSerialJSSC extends IODeviceAbstract {

  private final static Logger LOG = Logger.getLogger(IODeviceSerialJSSC.class.getName());

  private Timer refresher = new Timer(true);
  private SerialSender serialSender = new SerialSender();

  @Autowired
  BoblightConfiguration configuration;

  @Override
  void connect() throws IOException {
    String portName = configuration.getDevice().get("output");
    int rate = toInt(configuration.getDevice().get("rate"));
    LOG.info("Connecting to device: " + portName + " with " + rate + " baud");

    try {
      serialSender.port = new SerialPort(portName);
      if (serialSender.port.openPort() && serialSender.port.setParams(rate, 8, 1, 0)) {
        LOG.info("Port opened success");
      }

      refresher.schedule(serialSender, 200, 20);

      serialSender.port.addEventListener(new SerialPortEventListener() {
        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
  /*
          switch(serialPortEvent.getEventType()) {
            case SerialPortEvent.RXCHAR: LOG.info("EVENT RXCHAR"); break;
            case SerialPortEvent.RXFLAG: LOG.info("EVENT RXFLAG"); break;
            case SerialPortEvent.TXEMPTY: LOG.info("EVENT TXEMPTY");
              serialSender.canSend(true);
              break;
            case SerialPortEvent.CTS: LOG.info("EVENT CTS"); break;
            case SerialPortEvent.DSR: LOG.info("EVENT DSR"); break;
            case SerialPortEvent.RLSD: LOG.info("EVENT RLSD"); break;
            case SerialPortEvent.BREAK: LOG.info("EVENT BREAK"); break;
            case SerialPortEvent.ERR: LOG.info("EVENT ERR"); break;
            case SerialPortEvent.RING: LOG.info("EVENT RING"); break;
          }
  */
          //        LOG.info("Serial Event: " + serialPortEvent.getEventType());
          try {
            String line = serialSender.port.readString();
            if (line != null) {
              LOG.info("Arduino answer: " + line);
            }
            sleep(100);
          } catch (Exception e) {
          }
        }
      });
    } catch (SerialPortException e) {
      throw new IOException("Can't open " + portName + ": " + e.getMessage());
    }
    try { sleep(1000);} catch (InterruptedException e) { }
  }

  @PreDestroy
  public void destroy() throws Exception {
    serialSender.close();
  }

  protected void send(byte[] message) {
    serialSender.pushBack(message);
  }

  private class SerialSender extends TimerTask {
    private SerialPort port;
    private Queue<byte[]> messageQueue = new ConcurrentLinkedDeque<>();

    @Override
    public void run() {
      while (!messageQueue.isEmpty()) {
//        canSend = false;
        int blocks = messageQueue.size();
        blocks = Math.min(blocks, configuration.getMaxBlocks());
//        System.out.println("Blocks "+blocks);

        ByteBuffer buf = ByteBuffer.allocate(blocks * 4 + 2);
        buf.put((byte)'N');
        buf.put((byte)blocks);
        for(int i=0; i<blocks; i++) {
          buf.put(messageQueue.poll());
        }

/*
        for (byte b : buf.array()) {
          System.out.format("%02X ", b);
        }
        System.out.println();
*/

        send(buf.array());
        try {sleep(10);} catch (InterruptedException e) {}
      }
    }

    void close() throws SerialPortException {
      if (port != null) {
        port.closePort();
      }
    }

    private synchronized void send(byte[] bytes) {
      try {
        port.writeBytes(bytes);
      } catch (SerialPortException e) {
        LOG.log(Level.SEVERE, "Can't send to serial port: " + e.getMessage(), e);
      }
    }

    public void pushBack(byte[] message) {
      messageQueue.offer(message);
    }
  }
}