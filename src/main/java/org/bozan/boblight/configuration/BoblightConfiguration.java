package org.bozan.boblight.configuration;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang.math.NumberUtils;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Iterables.getFirst;
import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.math.NumberUtils.toInt;

public class BoblightConfiguration {

  private Multimap<String,Map<String, String>> boblightConfig = ArrayListMultimap.create();

  private static BoblightConfiguration instance;

  private BoblightConfiguration() {}

  public static BoblightConfiguration getInstance() throws IOException {
    if(instance == null) {
      instance = new BoblightConfiguration();
      instance.parseBoblightConfig(new FileInputStream(System.getProperty("config", "./config/boblight.conf")));
    }
    return instance;
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

      Optional<String> sectionName = parseSection(line);
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

  private Optional<String> parseSection(String line) {
    Pattern pattern = Pattern.compile("^\\[.*\\]$");
    Matcher matcher = pattern.matcher(line);
    if(matcher.matches()) {
      return Optional.of(substring(line, matcher.start() + 1, matcher.end() - 1));
    }
    return Optional.absent();
  }

  public int getProtocolVersion() {
    return toInt(getSection("global").get("protocolVersion"));
  }

  public int getPort() {
    return toInt(getSection("global").get("port"));
  }

  public int getLightOffset() {
    return toInt(getSection("global").get("lightOffset"));
  }

  public int getMaxBlocks() {
    return toInt(getSection("global").get("maxBlocks"));
  }

  public Collection<Map<String, String>> getLights() {
    return boblightConfig.get("light");
  }

  public Map<String, String> getDevice() {
    return getSection("device");
  }

  public Map<String, String> getSection(final String sectionName) {
    return getFirst(boblightConfig.get(sectionName), null);
  }
}
