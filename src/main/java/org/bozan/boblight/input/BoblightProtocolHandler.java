package org.bozan.boblight.input;

import java.nio.ByteBuffer;

public interface BoblightProtocolHandler {
  void handleMessage(String message, ResponseHandler responseHandler);

  static interface ResponseHandler {
     void onResponse(String response);
  }
}
