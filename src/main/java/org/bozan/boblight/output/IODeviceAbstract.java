package org.bozan.boblight.output;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

import static com.google.common.collect.Maps.newHashMap;

abstract class IODeviceAbstract implements IODevice {
  private final static Logger LOG = Logger.getLogger(IODevice.class.getName());

  private Map<Integer, Integer> activeLights = newHashMap();
  protected Queue<byte[]> messageQueue = new ConcurrentLinkedDeque<>();

  abstract void connect() throws IOException;

  @Override
  public void setLight(int ledId, int r, int g, int b) {
    int rgb = r;
    rgb <<= 8;
    rgb |= g;
    rgb <<= 8;
    rgb |= b;
    setLight(ledId, rgb);
  }

  @Override
  public void setLight(int ledId, int rgb) {
    if (!activeLights.containsKey(ledId) || activeLights.get(ledId) != rgb) {
      activeLights.put(ledId, rgb);
      messageQueue.offer(ByteBuffer.allocate(4)
          .putInt(rgb)
          .put(0, (byte) ledId)
          .array());
    }
  }

  protected void logData(byte[] data) {
    StringBuffer buffer = new StringBuffer("DATA:");

    for (byte b : data) {
      buffer.append(String.format(" %02X", b));
    }
    LOG.info(buffer.toString());
  }
}
