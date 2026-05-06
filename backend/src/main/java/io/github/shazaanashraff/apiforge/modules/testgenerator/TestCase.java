package io.github.shazaanashraff.apiforge.modules.testgenerator;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpMethod;

public record TestCase(
    String id,
    TestCategory category,
    String endpointPath,
    HttpMethod method,
    Map<String, String> headers,
    Map<String, String> queryParams,
    Map<String, String> pathParams,
    Object requestBody,
    List<Assertion> expectedAssertions,
    String description,
    boolean applicableIfMongoBacked) {}
