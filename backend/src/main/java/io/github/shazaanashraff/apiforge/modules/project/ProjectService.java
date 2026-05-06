package io.github.shazaanashraff.apiforge.modules.project;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Public API of the project module. Other modules interact with projects through this service —
 * never by importing ProjectRepository directly (Spring Modulith rule).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final ApiSpecRepository apiSpecRepository;

  /** Returns all projects for a tenant, newest first. RLS on the DB enforces tenant isolation. */
  @Transactional(readOnly = true)
  public List<Project> listProjects(UUID tenantId) {
    return projectRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
  }

  @Transactional(readOnly = true)
  public Project getProject(UUID projectId) {
    return projectRepository
        .findById(projectId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Project not found: " + projectId));
  }

  @Transactional
  public Project createProject(UUID tenantId, String name, String baseUrl, UUID createdBy) {
    Project project = new Project(tenantId, name, baseUrl);
    project.setCreatedBy(createdBy);
    Project saved = projectRepository.save(project);
    log.info(
        "Project created: id={}, name={}, tenantId={}", saved.getId(), saved.getName(), tenantId);
    return saved;
  }

  @Transactional
  public Project updateProject(UUID projectId, String name, String baseUrl, boolean isMongoBackedApi) {
    Project project = getProject(projectId);
    project.setName(name);
    project.setBaseUrl(baseUrl);
    project.setMongoBackedApi(isMongoBackedApi);
    return projectRepository.save(project);
  }

  @Transactional
  public void deleteProject(UUID projectId) {
    Project project = getProject(projectId);
    projectRepository.delete(project);
    log.info("Project deleted: id={}", projectId);
  }

  /** Returns the most recently uploaded spec for a project. */
  @Transactional(readOnly = true)
  public ApiSpec getLatestSpec(UUID projectId) {
    return apiSpecRepository
        .findTopByProjectIdOrderByCreatedAtDesc(projectId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No spec found for project: " + projectId));
  }

  @Transactional
  public ApiSpec saveSpec(ApiSpec spec) {
    return apiSpecRepository.save(spec);
  }
}
