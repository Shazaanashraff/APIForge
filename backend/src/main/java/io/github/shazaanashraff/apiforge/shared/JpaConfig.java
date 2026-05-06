package io.github.shazaanashraff.apiforge.shared;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Enables JPA auditing so @CreatedDate and @LastModifiedDate on BaseEntity are
 * populated automatically.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "io.github.shazaanashraff.apiforge")
public class JpaConfig {}
