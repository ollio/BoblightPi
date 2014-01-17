package org.bozan.boblight.output;

import jssc.SerialPortException;
import org.bozan.boblight.configuration.BoblightConfiguration;
import org.bozan.boblight.output.model.Color;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.Maps.newConcurrentMap;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Thread.sleep;

abstract class IODeviceAbstract implements IODevice {
  private final static Logger LOG = Logger.getLogger(IODevice.class.getName());

  private Map<Byte, Color> activeLights = newConcurrentMap();
  protected Queue<byte[]> messageQueue = new ConcurrentLinkedDeque<>();
  private final int maxBlocks;
  private Timer refresher = new Timer(true);
  protected final BoblightConfiguration configuration;

  protected IODeviceAbstract(BoblightConfiguration configuration) {
    this.configuration = configuration;
    this.maxBlocks = configuration.getMaxBlocks();
    refresher.schedule(new SendTask(), 1000, 100);
  }

  abstract void connect() throws IOException;

  @Override
  public final void setLight(byte ledId, byte r, byte g, byte b) {
//    int rgb = r;
//    rgb <<= 8;
//    rgb |= g;
//    rgb <<= 8;
//    rgb |= b;
//    setLight(ledId, rgb);
    activeLights.put(ledId, new Color(r, g, b));
  }

//  @Override
//  public final void setLight(int ledId, int rgb) {
//    if (!activeLights.containsKey(ledId) || activeLights.get(ledId) != rgb) {
//      activeLights.put(ledId, rgb);
//      messageQueue.offer(ByteBuffer.allocate(4)
//          .putInt(rgb)
//          .put(0, (byte) ledId)
//          .array());
//    }
//  }

  protected void logData(byte[] data) {
    StringBuffer buffer = new StringBuffer("DATA:");

    for (byte b : data) {
      buffer.append(String.format(" %02X", b));
    }
    LOG.info(buffer.toString());
  }

  protected abstract void writeBytes(byte[] array) throws Exception;

  private class SendTask extends TimerTask {
    /*
        @Override
        public synchronized void run() {
          while (!messageQueue.isEmpty()) {
            try {
              int blocks = messageQueue.size();
              blocks = Math.min(blocks, maxBlocks);

              ByteBuffer buf = ByteBuffer.allocate(blocks * 4 + 2);
              buf.put((byte) 'N');
              buf.put((byte) blocks);
              for (int i = 0; i < blocks; i++) {
                buf.put(messageQueue.poll());
              }
    //          logData(buf.array());
              if(messageQueue.size() > 10) {
                LOG.warning("QUEUE SIZE: " + messageQueue.size());
              }

              writeBytes(buf.array());
            } catch (Exception e1) {
              LOG.log(Level.SEVERE, "Can't send to device: " + e1.getMessage(), e1);
            }
            try {
              sleep(10);
            } catch (InterruptedException e) {
            }
          }
        }
    */
    @Override
    public synchronized void run() {
      int blocks = activeLights.size();
      ByteBuffer buf = ByteBuffer.allocate(blocks * 4 + 2);
      buf.put((byte) 'N');
      buf.put((byte) blocks);
      for (Map.Entry<Byte, Color> entry : activeLights.entrySet()) {
        buf.put(entry.getKey().byteValue());
        Color c = entry.getValue();
        buf.put(c.r);
        buf.put(c.g);
        buf.put(c.b);
      }
      LOG.info("Update " + buf.position());

      try {
        writeBytes(buf.array());
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "Can't send to device: " + e.getMessage());
      }
    }
  }
}
