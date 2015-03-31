package org.bozan.boblight.output.buffer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.netty.util.internal.ConcurrentSet;
import org.bozan.boblight.configuration.BoblightConfiguration;

import java.util.*;

public class LedBuffer {

  int tolerance = 10;

  public LedBuffer(BoblightConfiguration config) {
    this.tolerance = config.getColorTolerance();
  }

  HashMap<Byte, Led> leds = new HashMap<>();
  Set<Led> updatedLeds =  new ConcurrentSet<>();

  public void updateLed(Led led) {
    if (!leds.containsKey(led.id) || differentColor(leds.get(led.id), led)) {
      leds.put(led.id, led);
      updatedLeds.add(led);
    }
  }

  public List<Led> getUpdatedLeds() {
    List<Led> result = Lists.newArrayList();
    result.addAll(updatedLeds);
    updatedLeds.clear();
    return result;
  }

  public List<Led> getAllLeds() {
    List<Led> result = Lists.newArrayList();
    result.addAll(leds.values());
    result.addAll(getUpdatedLeds());
    return result;
  }

  private boolean differentColor(Led oldLed, Led newLed) {
    return oldLed.r - tolerance > newLed.r || oldLed.r + tolerance < newLed.r ||
        oldLed.g - tolerance > newLed.g || oldLed.g + tolerance < newLed.g ||
        oldLed.b - tolerance > newLed.b || oldLed.b + tolerance < newLed.b;
  }
}
