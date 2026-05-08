package io.github.shazaanashraff.sample.order;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Data
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(nullable = false)
  private Integer quantity = 1;

  @Column(name = "total_price", nullable = false)
  private BigDecimal totalPrice = BigDecimal.ZERO;

  @Column(name = "customer_name", nullable = false)
  private String customerName;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
