package org.bozan.boblight.output;

import com.google.common.collect.Lists;
import org.bozan.boblight.configuration.BoblightConfiguration;
import org.bozan.boblight.output.buffer.Led;
import org.bozan.boblight.output.buffer.LedBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class IODeviceAbstract implements IODevice {
  private final static Logger LOG = Logger.getLogger(IODevice.class.getName());

  private LedBuffer ledBuffer;

  //  private Queue<Led> messageQueue = new ConcurrentLinkedDeque<>();
  protected final BoblightConfiguration configuration;

  private final byte lightOffset;
  private final int maxBlocks;

  private Timer refresher = new Timer(true);

  protected IODeviceAbstract(BoblightConfiguration config) {
    this.configuration = config;
    this.ledBuffer = new LedBuffer(config);
    this.maxBlocks = config.getMaxBlocks();
    this.lightOffset = (byte) config.getLightOffset();
    int refreshDelay = 1000 / config.getFps();
    refresher.schedule(new SendTask(), 1000, refreshDelay);
  }

  abstract void connect() throws IOException;

  void disconnect() throws IOException {
  }

  ;

  @Override
  public final void setLight(byte ledId, byte r, byte g, byte b) {
    ledBuffer.updateLed(new Led((byte) (lightOffset + ledId), r, g, b));
  }

  protected void logData(byte[] data) {
    StringBuffer buffer = new StringBuffer("<< len[" + data.length + "] ");

    for (byte b : data) {
      buffer.append(String.format(" %02X", b));
    }
    LOG.info(buffer.toString());
  }

  protected abstract void writeBytes(byte[] array) throws Exception;

  private class SendTask extends TimerTask {
    @Override
    public synchronized void run() {
      List<List<Led>> partials = Lists.partition(ledBuffer.getUpdatedLeds(), maxBlocks);
      for (List<Led> partial : partials) {
        ByteBuffer buf = ByteBuffer.allocate(partial.size() * 4 + 2);
        buf.put((byte) 'N');
        buf.put((byte) partial.size());
        for (Led led : partial) {
          buf.put(led.array());
        }

//        LOG.info("partials " + partials.size() + "blocks: " + partial.size() + " len: " + buf.array().length);
//        logData(buf.array());
        try {
          writeBytes(buf.array());
        } catch (Exception e) {
          LOG.log(Level.SEVERE, "Buffer size: " + buf.array().length + " blocks: " + partial.size() + "\nError: " + e.getMessage());
        }

      }

/*
      while (!messageQueue.isEmpty()) {
        int blocks = messageQueue.size();
        blocks = Math.min(blocks, maxBlocks);

        ByteBuffer buf = ByteBuffer.allocate(blocks * 4 + 2);
        buf.put((byte) 'N');
        buf.put((byte) blocks);
        for (int i = 0; i < blocks; i++) {
          buf.put(messageQueue.poll().array());
        }
//        LOG.info("queue size: " + messageQueue.size() + " blocks: " + blocks + " len: " + (blocks * 4 + 2));
//        logData(buf.array());
        try {
          writeBytes(buf.array());
        } catch (Exception e) {
          LOG.log(Level.SEVERE, "Buffer size: " + buf.array().length + " queue size: " + messageQueue.size() + " blocks: " + blocks + "\nError: " + e.getMessage());
        }
      }
*/
    }
  }
}
