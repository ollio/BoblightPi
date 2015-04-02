package org.bozan.boblight.output;

import org.bozan.boblight.configuration.BoblightConfiguration;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Created by Olli on 31.03.15.
 */
public class HueRestClient implements IODevice {
  private final static Logger LOG = Logger.getLogger(HueRestClient.class.getName());

  final BoblightConfiguration configuration;

  final String hueBridgeUrl;

  final Map<Byte, String> lights = new HashMap<>();
  private Timer refresher = new Timer(true);

  public HueRestClient() throws IOException {
    configuration = BoblightConfiguration.getInstance();
    hueBridgeUrl = configuration.getHueBridgeUrl();

    refresher.schedule(new SendTask(), 1000, 500);
  }

  @Override
  public void setLight(byte light, byte r, byte g, byte b) {
    float[] hsb = new float[3];
    Color.RGBtoHSB(r & 0xFF, g & 0xFF, b & 0xFF, hsb);

    lights.put(light, "{\"on\":true," +
        "\"hue\":" + toHueValue(hsb[0], 65535) + "," +
        "\"sat\":" + toHueValue(hsb[1], 255) + "," +
        "\"bri\":" + toHueValue(hsb[2], 255) + "," +
        "\"transistiontime\":0}");
  }

  String toHueValue(float val, float max) {
    return String.format("%d", (int) (max * val));
  }

  private class SendTask extends TimerTask {
    @Override
    public void run() {
      for (Map.Entry<Byte, String> entry : lights.entrySet()) {
        updateLightState(entry.getKey(), entry.getValue());
      }
    }

    void updateLightState(byte light, String json) {
      LOG.info("light[" + light + "]: " + json);
      try {
        URL url = new URL(hueBridgeUrl + "/lights/" + light + "/state");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");

        Writer w = new OutputStreamWriter(connection.getOutputStream());
        w.write(json);
        w.flush();

        connection.disconnect();
        LOG.info(connection.getResponseMessage());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
