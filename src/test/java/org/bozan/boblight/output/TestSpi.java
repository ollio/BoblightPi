package org.bozan.boblight.output;

import com.pi4j.wiringpi.Spi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import static java.lang.Thread.sleep;

public class TestSpi {

 static final int CHANNEL = 1;

  public static void main(String[] args) throws Exception {
    System.out.println("Init SPI");

    int fd = Spi.wiringPiSPISetup(CHANNEL, 10000000);
    if (fd <= -1) {
      throw new IOException("SPI SETUP FAILED");
    }

    for (int i = 0; i < 1000; i++) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      PrintWriter out = new PrintWriter(bos);
      out.write("Hello World " + i + "\r\n");
      out.flush();

      byte[] packet = bos.toByteArray();
      logData(packet);
      Spi.wiringPiSPIDataRW(CHANNEL, packet, packet.length);
      sleep(2000);
    }
  }

  static void logData(byte[] data) {
    StringBuffer buffer = new StringBuffer("<< len["+data.length+"] ");

    for (byte b : data) {
      buffer.append(String.format(" %02X", b));
    }
    System.out.println(buffer.toString());
  }
}

