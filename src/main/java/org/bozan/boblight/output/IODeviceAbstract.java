package org.bozan.boblight.output;

import org.bozan.boblight.configuration.BoblightConfiguration;
import org.bozan.boblight.output.buffer.Led;
import org.bozan.boblight.output.buffer.LedBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Thread.sleep;

abstract class IODeviceAbstract implements IODevice {
  private final static Logger LOG = Logger.getLogger(IODevice.class.getName());

  private LedBuffer ledBuffer = new LedBuffer();

  private Queue<Led> messageQueue = new ConcurrentLinkedDeque<>();
  protected final BoblightConfiguration configuration;

  private final byte lightOffset;
  private final int maxBlocks;

  private Timer refresher = new Timer(true);

  protected IODeviceAbstract(BoblightConfiguration configuration) {
    this.configuration = configuration;
    this.maxBlocks = configuration.getMaxBlocks();
    this.lightOffset = (byte) configuration.getLightOffset();
    refresher.schedule(new SendTask(), 1000, 20);
  }

  abstract void connect() throws IOException;

  @Override
  public final void setLight(byte ledId, byte r, byte g, byte b) {
    Led led = new Led((byte) (lightOffset + ledId), r, g, b);
    if(ledBuffer.updateLed(led)) {
      messageQueue.offer(led);
    }
  }

  protected void logData(byte[] data) {
    StringBuffer buffer = new StringBuffer("<< len["+data.length+"] ");

    for (byte b : data) {
      buffer.append(String.format(" %02X", b));
    }
    LOG.info(buffer.toString());
  }

  protected abstract void writeBytes(byte[] array) throws Exception;

  private class SendTask extends TimerTask {
    @Override
    public synchronized void run() {
      while (!messageQueue.isEmpty()) {
        int blocks = messageQueue.size();
        blocks = Math.min(blocks, maxBlocks);

        ByteBuffer buf = ByteBuffer.allocate(blocks * 4 + 2);
        buf.put((byte) 'N');
        buf.put((byte) blocks);
        for (int i = 0; i < blocks; i++) {
          buf.put(messageQueue.poll().array());
        }

        LOG.info("queue size: " + messageQueue.size() + " blocks: " + blocks);
        logData(buf.array());
        try {
          writeBytes(buf.array());
        } catch (Exception e) {
          LOG.log(Level.SEVERE, "Buffer size: " + buf.array().length + " queue size: " + messageQueue.size() + " blocks: " + blocks + "\nError: " + e.getMessage());
        }
      }
    }
  }
}
