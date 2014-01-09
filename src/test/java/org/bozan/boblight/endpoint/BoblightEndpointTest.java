package org.bozan.boblight.endpoint;

import org.bozan.boblight.output.IODevice;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BoblightEndpointTest {

  BoblightEndpoint endpoint;

  @BeforeMethod
  public void setUp() throws Exception {
    endpoint = new BoblightEndpoint();
    endpoint.ioDevice = mock(IODevice.class);
  }

  @Test
  public void setLightMessage() throws Exception {
    endpoint.handleMessage("set light 62 rgb 0 0.564706 0.996078");

    verify(endpoint.ioDevice).setLight(62, 0, 144, 254);
  }
}
