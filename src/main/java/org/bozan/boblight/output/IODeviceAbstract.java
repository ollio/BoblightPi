package org.bozan.boblight.output;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

abstract class IODeviceAbstract implements IODevice {

  private Map<Integer, Integer> activeLights = newHashMap();

  abstract void connect() throws IOException;

  @Override
  public void setLight(int light, int r, int g, int b) {
    int rgb = r;
    rgb <<= 8;
    rgb |= g;
    rgb <<= 8;
    rgb |= b;

    if(!activeLights.containsKey(light) || activeLights.get(light) != rgb) {
      activeLights.put(light, rgb);
      send(ByteBuffer.allocate(4)
          .putInt(rgb)
          .put(0, (byte)light)
          .array());
    }
  }

  protected abstract void send(byte[] message);
}
