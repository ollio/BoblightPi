package org.bozan.boblight.output.buffer;

import org.apache.commons.lang.math.NumberUtils;

public class Led {
  public final byte id;
  public final byte r;
  public final byte g;
  public final byte b;

  public static Led Black(int id) {
    return new Led(id, 0, 0, 0);
  }

  public static Led White(int id) {
    return new Led(id, 254, 254, 254);
  }

  public static Led Red(int id) {
    return new Led(id, 254, 0, 0);
  }

  public static Led Green(int id) {
    return new Led(id, 0, 254, 0);
  }

  public static Led Blue(int id) {
    return new Led(id, 0, 0, 254);
  }

  public Led(Integer id, Integer r, Integer g, Integer b) {
    this(id.byteValue(), r.byteValue(), g.byteValue(), b.byteValue());
  }

  public Led(byte id, byte r, byte g, byte b) {
    this.id = id;
    this.r = r;
    this.g = g;
    this.b = b;
  }

  public int getRBG() {
    int rgb = r;
    rgb <<= 8;
    rgb |= g;
    rgb <<= 8;
    rgb |= b;
    return rgb;
  }

  public byte[] array() {
    return new byte[] {id, r, g, b};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Led led = (Led) o;

    if (id != led.id) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return (int) id;
  }
}
