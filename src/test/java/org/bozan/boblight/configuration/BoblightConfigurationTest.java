package org.bozan.boblight.configuration;

import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class BoblightConfigurationTest {

  BoblightConfiguration configuration = new BoblightConfiguration();


  @Test
  public void parseConfigFile() throws Exception {
    configuration.parseBoblightConfig(getClass().getResourceAsStream("boblight_test.conf"));

    assertThat(configuration.getLights()).isNotEmpty().hasSize(2);
    assertThat(configuration.getDevice()).isNotNull();
    assertThat(configuration.getDevice().get("name")).isEqualTo("device1");
    assertThat(configuration.getDevice().get("output")).isEqualTo("/dev/ttyACM0");
  }
}
