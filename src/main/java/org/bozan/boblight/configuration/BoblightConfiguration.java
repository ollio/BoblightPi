package org.bozan.boblight.configuration;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Iterables.getFirst;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.StringUtils.substring;

@Configuration
public class BoblightConfiguration {

  @Value("${boblight.protocolVersion}")
  private int protocolVersion;

  @Value("${boblight.port}")
  private int port;

  @Value("${boblight.lightOffset}")
  private int lightOffset;

  @Value("${boblight.maxBlocks}")
  private int maxBlocks;

  Multimap<String,Map<String, String>> boblightConfig = ArrayListMultimap.create();

  @Value("${boblight.config}")
  void parseBoblightConfig(File configFile) throws IOException {
    parseBoblightConfig(new FileInputStream(configFile));
  }

  void parseBoblightConfig(InputStream config) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(config));
    String line;
    Map<String, String> currentSectionConfig = null;
    String currentSection = null;
    while( (line = in.readLine()) != null ) {
      if(startsWith(line, "#") || isBlank(line)) {
        continue;
      }

      Optional<String> sectionName = getSection(line);
      if(sectionName.isPresent()) {
        if(currentSection != null && currentSectionConfig != null) {
          boblightConfig.put(currentSection, currentSectionConfig);
        }
        currentSectionConfig = new HashMap<>();
        currentSection = sectionName.get();
        continue;
      }

      Optional<Map.Entry<String, String>> entry = getMapEntry(line);
      if(entry.isPresent()) {
        currentSectionConfig.put(entry.get().getKey(), entry.get().getValue());
      }
    }
    boblightConfig.put(currentSection, currentSectionConfig);
  }

  private Optional<Map.Entry<String, String>> getMapEntry(String line) {
    Pattern pattern = Pattern.compile("(^\\S*)\\s*(.*$)");
    final Matcher matcher = pattern.matcher(line);
    Map.Entry<String, String> entry = null;
    if(matcher.matches()) {
      entry = new Map.Entry<String, String>() {
        @Override
        public String getKey() {
          return matcher.group(1);
        }

        @Override
        public String getValue() {
          return matcher.group(2);
        }

        @Override
        public String setValue(String value) {
          return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
      };
    }
    return Optional.fromNullable(entry);
  }

  private Optional<String> getSection(String line) {
    Pattern pattern = Pattern.compile("^\\[.*\\]$");
    Matcher matcher = pattern.matcher(line);
    if(matcher.matches()) {
      return Optional.of(substring(line, matcher.start() + 1, matcher.end() - 1));
    }
    return Optional.absent();
  }

  public int getProtocolVersion() {
    return protocolVersion;
  }

  public int getPort() {
    return port;
  }

  public int getLightOffset() {
    return lightOffset;
  }

  public int getMaxBlocks() {
    return maxBlocks;
  }

  public Collection<Map<String, String>> getLights() {
    return boblightConfig.get("light");
  }

  public Map<String, String> getDevice() {
    return getFirst(boblightConfig.get("device"), null);
  }
}
