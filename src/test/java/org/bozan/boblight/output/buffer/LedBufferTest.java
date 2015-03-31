package org.bozan.boblight.output.buffer;

import org.bozan.boblight.configuration.BoblightConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.bozan.boblight.output.buffer.Led.Black;
import static org.bozan.boblight.output.buffer.Led.White;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LedBufferTest {

  LedBuffer buffer;

  @BeforeMethod
  public void setUp() throws Exception {
    BoblightConfiguration configuration = mock(BoblightConfiguration.class);
    when(configuration.getColorTolerance()).thenReturn(10);

    buffer = new LedBuffer(configuration);
  }

  @DataProvider
  Object[][] leds() {
    return new Object[][]{
        {new Led(1, 50, 0, 0), new Led(1, 50, 0, 0), false},
        {new Led(1, 50, 0, 0), new Led(1, 51, 0, 0), false},
        {new Led(1, 50, 0, 0), new Led(1, 55, 0, 0), false},
        {new Led(1, 50, 0, 0), new Led(1, 70, 0, 0), true}
    };
  }

  @Test (dataProvider = "leds")
  public void colors_update_required(Led oldLed, Led newLed, boolean updateExpected) throws Exception {
    buffer.updateLed(oldLed);
    buffer.getUpdatedLeds();

    buffer.updateLed(newLed);

    assertThat(buffer.updatedLeds.contains(newLed)).isEqualTo(updateExpected);
  }

  @Test
  public void led_updated_after_required_update() throws Exception {
    buffer.updateLed(Black(1));
    buffer.updateLed(White(1));

    assertThat(buffer.leds.get((byte)1)).isEqualTo(White(1));
  }

  @Test
  public void getUpdatedLeds() throws Exception {
    buffer.updateLed(Black(1));
    buffer.updateLed(Black(2));
    buffer.updateLed(White(2));

    assertThat(buffer.getUpdatedLeds()).hasSize(2).contains(Black(1), White(2));
    assertThat(buffer.getUpdatedLeds()).isEmpty();
  }
}
