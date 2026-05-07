package io.github.shazaanashraff.apiforge.modules.loadtester;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PercentileCalculatorTest {

  @Test
  void p50IsMedianOfSortedList() {
    List<Long> sorted = List.of(10L, 20L, 30L, 40L, 50L);
    assertThat(PercentileCalculator.compute(sorted, 50)).isEqualTo(30L);
  }

  @Test
  void p95IsCorrectForTenElements() {
    List<Long> sorted = List.of(10L, 20L, 30L, 40L, 50L, 60L, 70L, 80L, 90L, 100L);
    assertThat(PercentileCalculator.compute(sorted, 95)).isEqualTo(100L);
  }

  @Test
  void p99ForSingleElementReturnsThatElement() {
    List<Long> sorted = List.of(42L);
    assertThat(PercentileCalculator.compute(sorted, 99)).isEqualTo(42L);
  }

  @Test
  void emptyListReturnsZero() {
    assertThat(PercentileCalculator.compute(List.of(), 50)).isEqualTo(0L);
  }
}
