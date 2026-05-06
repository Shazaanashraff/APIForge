package io.github.shazaanashraff.apiforge.modules.project;

import io.github.shazaanashraff.apiforge.shared.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A project groups an API spec and its test runs.
 *
 * <p>One project = one target API. You might have a project for "Java Sample API" and a separate
 * project for "Node Sample API".
 *
 * <p>The isMongoBackedApi flag triggers MongoDB-specific test generation (S07). The user can set
 * this explicitly in the UI, or the system auto-detects it from response IDs.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  /** The base URL of the target API, e.g. http://localhost:8090. Used by the executor. */
  @Column(name = "base_url", length = 2048)
  private String baseUrl;

  /**
   * When true, the test generator includes MongoDB-specific tests (ObjectId validation, NoSQL
   * injection). Can be set by the user or auto-detected by MongoBackedApiDetector.
   */
  @Column(name = "is_mongo_backed_api", nullable = false)
  private boolean isMongoBackedApi = false;

  @Column(name = "created_by")
  private UUID createdBy;

  public Project(UUID tenantId, String name, String baseUrl) {
    this.tenantId = tenantId;
    this.name = name;
    this.baseUrl = baseUrl;
  }
}
