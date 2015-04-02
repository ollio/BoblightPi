package org.bozan.boblight.output;

import com.philips.lighting.hue.sdk.*;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bozan.boblight.configuration.BoblightConfiguration;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static org.apache.commons.lang.StringUtils.substringAfterLast;

/**
 * Created by Olli on 02.04.15.
 */
public class HueSdkClient implements IODevice {
  private final static Logger LOG = Logger.getLogger(HueSdkClient.class.getName());

  final PHHueSDK phHueSDK;
  final BoblightConfiguration configuration;

  public HueSdkClient() throws IOException, InterruptedException {

    phHueSDK = PHHueSDK.getInstance();
    phHueSDK.setAppName("Boblight");
    phHueSDK.setDeviceName("RaspberryPi");
    phHueSDK.getNotificationManager().registerSDKListener(new MyPHSDKListener());

    configuration = BoblightConfiguration.getInstance();
    URL bridgeUrl = new URL(configuration.getHueBridgeUrl());
    connect(bridgeUrl.getHost(), substringAfterLast(bridgeUrl.getPath(), "/"));
  }

  private void connect(String ipAddress, String username) throws InterruptedException {
    PHAccessPoint accessPoint = new PHAccessPoint();
    accessPoint.setIpAddress(ipAddress);
    accessPoint.setUsername(username);
    phHueSDK.connect(accessPoint);

    while (getBridge() == null) {
      Thread.sleep(100);
    }
  }

  @Override
  public void setLight(byte light, byte r, byte g, byte b) {
    getBridge().getResourceCache().getAllLights().stream().filter(phLight ->
            NumberUtils.toByte(phLight.getIdentifier()) == light
    ).findFirst().ifPresent(phLight ->
        updateLightState(phLight, r, g, b)
    );
  }

  private PHBridge getBridge() {
    return phHueSDK.getSelectedBridge();
  }

  void updateLightState(PHLight light, byte r, byte g, byte b) {
    float xy[] = PHUtilities.calculateXYFromRGB(r & 0xFF, g & 0xFF, b & 0xFF, light.getModelNumber());
    PHLightState lightState = new PHLightState();
    lightState.setX(xy[0]);
    lightState.setY(xy[1]);
    getBridge().updateLightState(light, lightState);
  }

  private class MyPHSDKListener implements PHSDKListener {
    @Override
    public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
    }

    @Override
    public void onBridgeConnected(PHBridge phBridge) {
      phHueSDK.setSelectedBridge(phBridge);
      phHueSDK.enableHeartbeat(phBridge, 1000);
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
    }

    @Override
    public void onAccessPointsFound(List<PHAccessPoint> list) {
      phHueSDK.connect(list.get(0));
    }

    @Override
    public void onError(int i, String s) {
      LOG.warning("Error: " + s + " Code: " + i);
    }

    @Override
    public void onConnectionResumed(PHBridge phBridge) {
      LOG.info("onConnectionResumed: " + phBridge);
    }

    @Override
    public void onConnectionLost(PHAccessPoint phAccessPoint) {
      LOG.info("onConnectionLost: " + phAccessPoint);
    }

    @Override
    public void onParsingErrors(List<PHHueParsingError> list) {
      LOG.warning("onParsingErrors: " + list);
    }
  }
}
