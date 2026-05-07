package io.github.shazaanashraff.apiforge.modules.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableStore {

  private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)\\}");
  private final Map<String, String> store = new HashMap<>();

  public void put(String name, String value) {
    store.put(name, value);
  }

  public String get(String name) {
    return store.get(name);
  }

  public String interpolate(String template) {
    if (template == null) return null;
    Matcher m = PLACEHOLDER.matcher(template);
    StringBuilder sb = new StringBuilder();
    while (m.find()) {
      String var = m.group(1);
      String replacement = store.getOrDefault(var, m.group(0));
      m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    m.appendTail(sb);
    return sb.toString();
  }
}
