package org.bozan.boblight.input;

import org.apache.commons.lang.StringUtils;
import org.bozan.boblight.configuration.BoblightConfiguration;
import org.bozan.boblight.output.IODevice;
import org.bozan.boblight.output.IODeviceFactory;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.Math.round;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.math.NumberUtils.toDouble;
import static org.apache.commons.lang.math.NumberUtils.toInt;

/**
 * = hello =
 * This is the connection command. Hello command should return 'hello' from the server
 * {{{
 * >>hello
 * hello
 * }}}
 * <p/>
 * <p/>
 * = ping =
 * This command check if this client is currently used by any device.
 * {{{
 * >>ping
 * ping 1 #at least one device is using a light declared by this client
 * ping 0 #no light is currently in use
 * }}}
 * <p/>
 * = get =
 * This command is used the request the server. Two things can be asked: version and lights
 * ==version==
 * returns the protocol version used by the server, current version is 5.
 * <p/>
 * {{{
 * >>get version
 * version <protocol_version>
 * }}}
 * <p/>
 * ==lights==
 * returns the lights configuration declared in server configuration.
 * First line is the number of lights, then each lines correspond to one light and its scanning parameters.
 * <p/>
 * {{{
 * >>get lights
 * lights <nb_of_light>
 * light <name_of_light> scan <Vscan1> <Vscan2> <Hscan1> <Hscan2>
 * light <name_of_light> scan <Vscan1> <Vscan2> <Hscan1> <Hscan2>
 * light <name_of_light> scan <Vscan1> <Vscan2> <Hscan1> <Hscan2>
 * light <name_of_light> scan <Vscan1> <Vscan2> <Hscan1> <Hscan2>
 * }}}
 * <p/>
 * = set =
 * This command is used to change lights and client parameters.
 * They have no return.
 * <p/>
 * ==priority==
 * change the client priority, from 0 to 255, default is 128. The highest priority is the lowest number
 * {{{
 * >>set priority <new_value>
 * }}}
 * <p/>
 * ==light==
 * ===rgb===
 * Change the wanted color of one light for the given rgb value.
 * {{{
 * >>set light <name_of_light> rgb <float_r> <float_g> <float_b>
 * }}}
 * <p/>
 * ===speed===
 * Change the transition speed of one light.Value is between 0.0 and 100.0.
 * 100 means immediate changes.
 * {{{
 * >>set light <name_of_light> speed <float_speed>
 * }}}
 * <p/>
 * ===interpolation===
 * Enable or disable color interpolation between 2 step. Value is a boolean ("0"/"1" or "true"/"false")
 * {{{
 * >>set light <name_of_light> interpolation <boolean>
 * }}}
 * <p/>
 * ===use===
 * Declare that we do not use this light or not. By default we use all lights. All color change request will be ignored for a light declared as unused.
 * {{{
 * >>set light <name_of_light> use <boolean>
 * }}}
 * <p/>
 * ===singlechange===
 * I'm not sure.
 * the size of a step between the current value and the wanted value.
 * 0.5 means that the device will apply 50% of the difference between the two colors. 1.0 will apply the wanted color now.
 * {{{
 * >>set light <name_of_light> singlechange <float>
 * }}}
 * <p/>
 * = sync =
 * Send synchronised signal to wake the devices and tell them data is ready to be read.
 * 'allowsync' must be enabled in configuration file. Ignored if not allowsync or not synchronized device. Should be sent after each bulk set.
 * {{{
 * >>sync
 * #no return
 * }}}
 * <p/>
 * A typical client/server exchange may looks like this :
 * {{{
 * >>hello
 * hello
 * >>get version
 * version 5
 * <p/>
 * >>get lights
 * lights 4
 * light right scan 0 100 50 100
 * light left scan 0 100 0 50
 * light center scan 33.3 66.6 33.3 66.6
 * light top scan 0 50 0 100
 * <p/>
 * >>set light right 0.5 0.5 0.5 0.5\
 * set light left 0.5 0.5 0.5 0.5\
 * set light center 0.5 0.5 0.5 0.5\
 * set light top 0.5 0.5 0.5 0.5\
 * sync
 * <p/>
 * #if we want to check if our output is used, send a ping message
 * >>ping
 * ping 1
 * }}}
 */

public class BoblightProtocolHandlerImpl implements BoblightProtocolHandler {

  private final static Logger LOG = Logger.getLogger(BoblightProtocolHandlerImpl.class.getName());

  public static final String HELLO = "hello";
  public static final String GET = "get";
  public static final String SET = "set";
  public static final String SYNC = "sync";
  public static final String PING = "ping";
  public static final String VERSION = "version";
  public static final String LIGHTS = "lights";
  public static final String LIGHT = "light";

  final BoblightConfiguration configuration;

  final IODeviceFactory ioDeviceFactory;

  IODevice ioDevice = null;

  public BoblightProtocolHandlerImpl() throws IOException {
    configuration = BoblightConfiguration.getInstance();
    ioDeviceFactory = new IODeviceFactory();
    initIoDevice();
  }

  void initIoDevice() throws IOException {
    LOG.info("Endpoint listening on port: " + configuration.getPort());
    ioDevice = ioDeviceFactory.getIODevice();
  }

  @Override
  public void handleMessage(String request, ResponseHandler responseHandler) {
    if(isNotBlank(request)) {
      String response = handleMessage(request);
      if(isNotBlank(response)) {
        responseHandler.onResponse(response);
      };
    }
  }

  String handleMessage(String input) {
    String[] command = StringUtils.split(input);

    switch (command[0]) {
      case HELLO:
        return input;
      case GET:
        return handleGet(command);
      case SET:
        return handleSet(command);
      case SYNC:
        return null;
      case PING:
        return PING + " 1";
    }

    return null;
  }

  private String handleGet(String[] command) {
    switch (command[1]) {
      case VERSION:
        return VERSION + ' ' + configuration.getProtocolVersion();
      case LIGHTS:
        return getLights();
    }
    return null;
  }

  private String handleSet(String[] command) {
    switch (command[1]) {
      case LIGHT:
        return setLight(command);
    }
    return null;
  }

  //set light 62 rgb 0 0.564706 0.996078
  private String setLight(String[] command) {
    ioDevice.setLight(toInt(command[2]), ftoi(command[4]), ftoi(command[5]), ftoi(command[6]));
    return null;
  }

  private int ftoi(String val) {
    return (int) round(toDouble(val) * 255.0);
  }

  private String getLights() {
    StringBuffer result = new StringBuffer("lights " + configuration.getLights().size() + "\n");
    for (Map<String, String> light : configuration.getLights()) {
      result.append(" light " + light.get("name") + " scan " + light.get("vscan") + " " + light.get("hscan") + "\n");
    }
    return result.substring(0, result.length() - 1);
  }

}
