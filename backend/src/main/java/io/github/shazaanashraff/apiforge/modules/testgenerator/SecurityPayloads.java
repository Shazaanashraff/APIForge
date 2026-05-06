package io.github.shazaanashraff.apiforge.modules.testgenerator;

import java.util.List;

/** Standard security test payloads for authorized API security testing. */
final class SecurityPayloads {

  private SecurityPayloads() {}

  static final List<String> SQL_INJECTION =
      List.of(
          "' OR '1'='1",
          "'; DROP TABLE users; --",
          "' UNION SELECT null,null,null --",
          "1' AND 1=1 --",
          "\" OR \"1\"=\"1");

  static final List<String> XSS =
      List.of(
          "<script>alert('xss')</script>",
          "<img src=x onerror=alert(1)>",
          "javascript:alert(1)",
          "<svg onload=alert(1)>",
          "'\"><script>alert(1)</script>");

  static final List<String> PATH_TRAVERSAL =
      List.of(
          "../etc/passwd",
          "../../etc/shadow",
          "..\\..\\windows\\system32\\",
          "%2e%2e%2f%2e%2e%2f",
          "....//....//etc/passwd");

  static final List<String> NOSQL_INJECTION =
      List.of(
          "{\"$gt\": \"\"}",
          "{\"$ne\": null}",
          "{\"$where\": \"1==1\"}",
          "{\"$regex\": \".*\"}",
          "[$ne]=1");

  static final List<String> NOSQL_INJECTION_QUERY_PARAM =
      List.of("[$ne]=1", "[$gt]=", "[$regex]=.*", "[$where]=1==1");
}
