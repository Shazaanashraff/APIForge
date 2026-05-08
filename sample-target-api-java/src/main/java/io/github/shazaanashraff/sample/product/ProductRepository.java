package io.github.shazaanashraff.sample.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  Page<Product> findAll(Pageable pageable);

  // BUG B9: raw string concatenation — SQL injection via JPQL
  @Query("FROM Product p WHERE p.name LIKE CONCAT('%', :q, '%') OR p.description LIKE CONCAT('%', :q, '%')")
  java.util.List<Product> searchByName(@Param("q") String q);
}
