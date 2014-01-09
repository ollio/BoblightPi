package org.bozan.boblight.controller;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.bozan.boblight.configuration.BoblightConfiguration;
import org.bozan.boblight.output.IODevice;
import org.bozan.boblight.output.IODeviceFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Created with IntelliJ IDEA.
 * User: bozan
 * Date: 09.01.14
 * Time: 23:17
 */

@Controller()
public class SimpleLightsController {

  @Inject
  BoblightConfiguration configuration;

  @Inject
  IODeviceFactory ioDeviceFactory;

  private IODevice ioDevice;

  @PostConstruct
  void info() throws IOException {
    ioDevice = ioDeviceFactory.getIODevice();
  }

  @RequestMapping(value = "/lights", method = GET)
  @ResponseStatus(OK)
  @ResponseBody
  public Iterable<String> getLights() {
    List<String> result = new ArrayList<>();
    for (Map<String, String> light : configuration.getLights()) {
      result.add(light.get("name"));
    }
    return result;
  }

  @RequestMapping(value = "/lights/{id}/{rgb}", method = PUT)
  @ResponseStatus(OK)
  public void putLights(@PathVariable("id") Integer id, @PathVariable("rgb") String rgb) {
    ioDevice.setLight(id, Integer.parseInt(rgb, 16));
  }
}
