package io.github.shazaanashraff.apiforge.modules.testgenerator;

import io.github.shazaanashraff.apiforge.modules.datagenerator.DataGenerator;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;

public record TestGenerationContext(
    Endpoint endpoint,
    DataGenerator dataGenerator,
    boolean mongoBackedApi,
    int rateLimitBurstCount,
    long defaultSlaThresholdMs) {

  public static TestGenerationContext of(Endpoint endpoint) {
    return new TestGenerationContext(endpoint, new DataGenerator(42L), false, 20, 2000L);
  }

  public static TestGenerationContext of(Endpoint endpoint, DataGenerator dataGenerator) {
    return new TestGenerationContext(endpoint, dataGenerator, false, 20, 2000L);
  }

  public static TestGenerationContext forMongo(Endpoint endpoint) {
    return new TestGenerationContext(endpoint, new DataGenerator(42L), true, 20, 2000L);
  }
}
