package io.github.shazaanashraff.apiforge.modules.executor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRunRepository extends JpaRepository<TestRun, UUID> {

  List<TestRun> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

  List<TestRun> findByProjectIdAndStatus(UUID projectId, TestRun.TestRunStatus status);
}
