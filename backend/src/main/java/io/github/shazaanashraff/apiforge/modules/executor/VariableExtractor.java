package io.github.shazaanashraff.apiforge.modules.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class VariableExtractor {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private VariableExtractor() {}

  public static String extract(String jsonPath, String responseBody) {
    try {
      String path = jsonPath.startsWith("$.") ? jsonPath.substring(2) : jsonPath;
      JsonNode node = MAPPER.readTree(responseBody);
      for (String segment : path.split("\\.")) {
        if (node == null || node.isMissingNode()) return null;
        if (segment.contains("[")) {
          String field = segment.substring(0, segment.indexOf('['));
          int index =
              Integer.parseInt(segment.substring(segment.indexOf('[') + 1, segment.indexOf(']')));
          node = node.path(field).path(index);
        } else {
          node = node.path(segment);
        }
      }
      return (node == null || node.isMissingNode() || node.isNull()) ? null : node.asText();
    } catch (Exception e) {
      return null;
    }
  }
}
