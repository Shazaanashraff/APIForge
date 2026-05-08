package io.github.shazaanashraff.sample.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders")
public class OrderController {

  private final OrderRepository repo;

  public OrderController(OrderRepository repo) {
    this.repo = repo;
  }

  // BUG B5: no body size limit — accepts payload > 1 MB without 413
  @PostMapping
  @Operation(summary = "Create order")
  public ResponseEntity<Order> create(@RequestBody Order order) {
    order.setId(null);
    order.setCreatedAt(Instant.now());
    return ResponseEntity.status(201).body(repo.save(order));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get order by ID")
  public ResponseEntity<Order> getById(@PathVariable Long id) {
    return repo.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
