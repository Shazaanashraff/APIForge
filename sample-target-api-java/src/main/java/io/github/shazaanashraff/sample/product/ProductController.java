package io.github.shazaanashraff.sample.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@Tag(name = "Products")
public class ProductController {

  private final ProductRepository repo;

  public ProductController(ProductRepository repo) {
    this.repo = repo;
  }

  // BUG B3 + B4: no pagination metadata (total/hasNext); no cap on size param
  @GetMapping
  @Operation(summary = "List products")
  public List<Product> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    // BUG B4: size is not capped — ?size=99999 returns all records
    return repo.findAll(PageRequest.of(page, size)).getContent();
    // Correct: should return Page<Product> with total, hasNext
  }

  // BUG B1: returns 200 instead of 201
  @PostMapping
  @Operation(summary = "Create product")
  public ResponseEntity<Product> create(@RequestBody Product product) {
    product.setId(null);
    product.setCreatedAt(Instant.now());
    product.setUpdatedAt(Instant.now());
    Product saved = repo.save(product);
    // BUG B1: should be ResponseEntity.status(201).body(saved)
    return ResponseEntity.ok(saved);
  }

  // BUG B2: throws 500 instead of 404 for missing ID
  @GetMapping("/{id}")
  @Operation(summary = "Get product by ID")
  public Product getById(@PathVariable Long id) {
    // BUG B2: .get() throws NoSuchElementException → 500; should return 404
    return repo.findById(id).get();
  }

  // BUG B10: updatedAt is not set on response entity
  @PutMapping("/{id}")
  @Operation(summary = "Update product")
  public ResponseEntity<Map<String, Object>> update(
      @PathVariable Long id, @RequestBody Product patch) {
    Product existing = repo.findById(id).orElseThrow();
    existing.setName(patch.getName());
    existing.setDescription(patch.getDescription());
    existing.setPrice(patch.getPrice());
    existing.setStock(patch.getStock());
    existing.setUpdatedAt(Instant.now());
    Product saved = repo.save(existing);
    // BUG B10: omits updatedAt from response
    return ResponseEntity.ok(Map.of(
        "id", saved.getId(),
        "name", saved.getName(),
        "description", saved.getDescription() == null ? "" : saved.getDescription(),
        "price", saved.getPrice(),
        "stock", saved.getStock(),
        "createdAt", saved.getCreatedAt().toString()
        // updatedAt intentionally omitted
    ));
  }

  // BUG B7: second DELETE throws 500 instead of 404/204
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete product")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    // BUG B7: .get() on second call → NoSuchElementException → 500
    Product product = repo.findById(id).get();
    repo.delete(product);
    return ResponseEntity.noContent().build();
  }

  // BUG B6: 3-second sleep violates SLA
  @GetMapping("/search")
  @Operation(summary = "Search products (slow)")
  public List<Product> search(@RequestParam(defaultValue = "") String q) {
    try {
      Thread.sleep(3000); // BUG B6: deliberate 3-second SLA violation
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return repo.searchByName(q);
  }
}
