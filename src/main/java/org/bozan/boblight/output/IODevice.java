package org.bozan.boblight.output;

public interface IODevice {

  void setLight(byte ledId, byte r, byte g, byte b);

//  void setLight(int ledId, int rgb);
}
