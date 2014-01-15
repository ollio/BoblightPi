package org.bozan.boblight.output;

import org.bozan.boblight.configuration.BoblightConfiguration;
import org.testng.annotations.*;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IODeviceSerialJSSCTest {

  IODeviceSerialJSSC controller;

  @BeforeTest
  public void setUp() throws Exception {
    BoblightConfiguration configuration = mock(BoblightConfiguration.class);
    controller = new IODeviceSerialJSSC(configuration);

    Map<String, String> device = new HashMap<>();
    device.put("output", "COM3");
    device.put("rate", "115200");
    when(configuration.getDevice()).thenReturn(device);
    when(configuration.getMaxBlocks()).thenReturn(40);

    controller.connect();
  }

  @AfterTest
  public void tearDown() throws Exception {
    sleep(2000);
    controller.destroy();
  }

  @DataProvider
  Object[][]colors() {
     return new Object[][] {
         {20, 200, 200, 50},
         {120, 20, 200, 40},
         {200, 20, 20, 30},
         {0, 200, 20, 20},
         {240, 200, 20, 10},
         {240, 0, 220, 5},
     };
  }

  @Test(invocationCount = 4, dataProvider = "colors")
  public void testSetLight(int r, int g, int b, int speed) throws Exception {
    for(int i=0; i<64; i++) {
      controller.setLight(i, r, g, b);
      if(i>0) {
        controller.setLight(i-1, 0, 0, 0);
      }
      sleep(speed);
    }
    for(int i=64; i>0; i--) {
      controller.setLight(i, r, g, b);
      controller.setLight(i+1, 0, 0, 0);
      sleep(speed);
    }

//    sleep(1000);
  }
}
