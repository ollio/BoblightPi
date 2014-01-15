package org.bozan.boblight.input;

import org.bozan.boblight.configuration.BoblightConfiguration;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class BoblightServerStandard implements BoblightServer {

  private final static Logger LOG = Logger.getLogger(BoblightServerStandard.class.getName());

  private BoblightProtocolHandler protocolHandler;
  private ServerSocket serverSocket;
  private ExecutorService executor;

  public BoblightServerStandard() throws IOException {
    executor = Executors.newCachedThreadPool();
    protocolHandler = new BoblightProtocolHandlerImpl();
    serverSocket = initServerSocket();
  }

  public ServerSocket initServerSocket() throws IOException {
    int port = BoblightConfiguration.getInstance().getPort();

    return new ServerSocket(port);
  }


  @Override
  public void run() {
    while (true) {
      try {
        final Socket socket = serverSocket.accept();
        executor.execute(new Client(socket));
      } catch (IOException e) {
        LOG.severe("Client exception: " + e.getMessage());
      }
    }

  }

  private class Client implements Runnable, BoblightProtocolHandler.ResponseHandler {
    private final BufferedReader in;
    private final BufferedWriter out;
    private Socket socket;

    private Client(Socket socket) throws IOException {
      this.socket = socket;
      this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
      try {
        while(true) {
          String line;
          while((line = in.readLine()) != null) {
            protocolHandler.handleMessage(line, this);
          }
          try { sleep(100); } catch (InterruptedException e) {}
        }
      } catch (IOException e) {
        LOG.severe("Error reading message: " + e.getMessage());
      } finally {
        close();
      }
    }

    @Override
    public void onResponse(String response) {
      try {
        out.write(response);
        out.newLine();
        out.flush();
      } catch (IOException e) {
        LOG.severe("Error writing message: " + e.getMessage());
        close();
      }
    }

    private void close() {
      try {
        in.close();
        out.close();
        socket.close();
      } catch (IOException e1) {}
    }
  }
}
