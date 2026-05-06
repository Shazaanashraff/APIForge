package io.github.shazaanashraff.apiforge.modules.project;

import io.github.shazaanashraff.apiforge.shared.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores the raw spec content associated with a project.
 *
 * <p>When a user uploads a new spec, a new ApiSpec is created. The project can have multiple specs
 * over time (history), but the most recently parsed one is used for test generation.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "api_specs")
public class ApiSpec extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, length = 50)
  private SourceType sourceType;

  @Column(name = "source_url", length = 2048)
  private String sourceUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "spec_format", nullable = false, length = 20)
  private SpecFormat specFormat;

  @Column(name = "spec_content", nullable = false, columnDefinition = "TEXT")
  private String specContent;

  @Column(name = "endpoint_count")
  private Integer endpointCount;

  @Column(name = "parsed_at")
  private Instant parsedAt;

  public enum SourceType {
    FILE,
    URL,
    INTROSPECT,
    POSTMAN
  }

  public enum SpecFormat {
    OPENAPI_JSON,
    OPENAPI_YAML,
    POSTMAN
  }
}
