package org.bozan.boblight.output.buffer;

import java.util.HashMap;

public class LedBuffer {

  static final int TOLERANCE = 10;

  HashMap<Byte, Led> leds = new HashMap<>();

  public boolean updateLed(Led led) {
    if (!leds.containsKey(led.id) || differentColor(leds.get(led.id), led)) {
      leds.put(led.id, led);
      return true;
    }
    return false;
  }

  private boolean differentColor(Led oldLed, Led newLed) {
    return oldLed.r - TOLERANCE > newLed.r || oldLed.r + TOLERANCE < newLed.r ||
        oldLed.g - TOLERANCE > newLed.g || oldLed.g + TOLERANCE < newLed.g ||
        oldLed.b - TOLERANCE > newLed.b || oldLed.b + TOLERANCE < newLed.b;
  }

}
