package org.bozan.boblight.output.buffer;

import org.bozan.boblight.configuration.BoblightConfiguration;

import java.util.HashMap;

public class LedBuffer {

  int tolerance = 2;

  BoblightConfiguration config;

  public LedBuffer(BoblightConfiguration config) {
    this.config = config;

    this.tolerance = config.getColorTolerance();
  }

  HashMap<Byte, Led> leds = new HashMap<>();

  public boolean updateLed(Led led) {
    if (!leds.containsKey(led.id) || differentColor(leds.get(led.id), led)) {
      leds.put(led.id, led);
      return true;
    }
    return false;
  }

  private boolean differentColor(Led oldLed, Led newLed) {
    return oldLed.r - tolerance > newLed.r || oldLed.r + tolerance < newLed.r ||
        oldLed.g - tolerance > newLed.g || oldLed.g + tolerance < newLed.g ||
        oldLed.b - tolerance > newLed.b || oldLed.b + tolerance < newLed.b;
  }
}
