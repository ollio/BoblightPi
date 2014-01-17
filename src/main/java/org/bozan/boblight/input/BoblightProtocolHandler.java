package org.bozan.boblight.input;

public interface BoblightProtocolHandler {
  void handleMessage(String message, ResponseHandler responseHandler);

  static interface ResponseHandler {
     void onResponse(String response);
  }
}
