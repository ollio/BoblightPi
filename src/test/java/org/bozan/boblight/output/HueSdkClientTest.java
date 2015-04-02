package org.bozan.boblight.output;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.awt.*;

public class HueSdkClientTest {

  HueSdkClient hue;

  @BeforeClass
  public void setUp() throws Exception {
    hue = new HueSdkClient();

  }

  @DataProvider
  public Object[][] colors() {
    return new Object[][] {
        new Color[] {Color.RED},
        new Color[] {Color.YELLOW},
        new Color[] {Color.GREEN},
        new Color[] {Color.CYAN},
        new Color[] {Color.BLUE},
        new Color[] {Color.PINK},
        new Color[] {Color.MAGENTA},
        new Color[] {Color.WHITE},
        new Color[] {Color.BLACK},
    };
  }

  @Test(dataProvider = "colors")
  public void setLight(Color col) throws Exception {
    hue.setLight((byte)1, (byte)col.getRed(), (byte)col.getGreen(), (byte)col.getBlue());
    hue.setLight((byte)2, (byte)col.getRed(), (byte)col.getGreen(), (byte)col.getBlue());
    hue.setLight((byte)3, (byte)col.getRed(), (byte)col.getGreen(), (byte)col.getBlue());
    hue.setLight((byte)4, (byte)col.getRed(), (byte)col.getGreen(), (byte)col.getBlue());

    Thread.sleep(1000);
  }
}