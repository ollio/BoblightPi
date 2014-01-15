package org.bozan.boblight;

import org.bozan.boblight.input.BoblightServer;
import org.bozan.boblight.input.BoblightServerNetty;
import org.bozan.boblight.input.BoblightServerStandard;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class BoblightPiApp {

  private final static Logger LOG = Logger.getLogger(BoblightPiApp.class.getName());

  private ExecutorService executor;
  private BoblightServer boblightServer;

  private void start() throws IOException {
    startBoblightServer();
  }

  private void startBoblightServer() throws IOException {
    LOG.info("Starting Server..");
    boblightServer = new BoblightServerNetty();
    executor = Executors.newCachedThreadPool();
    executor.execute(boblightServer);
  }


  public static void main(String[] args) throws IOException {
    new BoblightPiApp().start();
  }
}
