package org.bozan.boblight.output.model;

public class Color {
  public final byte r;
  public final byte g;
  public final byte b;

  public Color(byte r, byte g, byte b) {
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
}
