package org.bozan.boblight.input;

import org.bozan.boblight.output.IODevice;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BoblightProtocolHandlerImplTest {

  BoblightProtocolHandlerImpl protocolHandler;

  @BeforeMethod
  public void setUp() throws Exception {
    protocolHandler = new BoblightProtocolHandlerImpl();
    protocolHandler.ioDevice = mock(IODevice.class);
  }

  @Test
  public void setLightMessage() throws Exception {
    protocolHandler.handleMessage("set light 62 rgb 0 0.564706 0.996078");

//    verify(protocolHandler.ioDevice).setLight(62, 0, 144, 254);
  }
}
