package io.github.shazaanashraff.apiforge.modules.testgenerator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.IdFormatHint;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MongoBackedApiDetector {

  private static final Pattern OBJECTID_PATTERN = Pattern.compile("[0-9a-fA-F]{24}");

  /**
   * Returns true when the endpoint set looks MongoDB-backed.
   * Checks idFormatHint first; falls back to scanning response schema JSON for 24-char hex IDs.
   */
  public boolean isMongoBacked(List<Endpoint> endpoints) {
    return endpoints.stream().anyMatch(this::looksMongoEndpoint);
  }

  public boolean looksMongoEndpoint(Endpoint ep) {
    if (ep.idFormatHint() != null
        && ep.idFormatHint().format() == IdFormatHint.Format.OBJECTID) {
      return true;
    }
    if (ep.responses() != null) {
      return ep.responses().values().stream()
          .filter(r -> r.schemaJson() != null)
          .anyMatch(r -> containsObjectId(r.schemaJson()));
    }
    return false;
  }

  private boolean containsObjectId(String schemaJson) {
    Matcher m = OBJECTID_PATTERN.matcher(schemaJson);
    return m.find();
  }
}
