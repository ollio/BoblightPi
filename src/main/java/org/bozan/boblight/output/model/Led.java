package org.bozan.boblight.output.model;

public class Led {
  public final byte id;
  public final byte r;
  public final byte g;
  public final byte b;

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
