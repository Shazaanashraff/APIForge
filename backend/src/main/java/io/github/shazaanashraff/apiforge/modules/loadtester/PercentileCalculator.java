package io.github.shazaanashraff.apiforge.modules.loadtester;

import java.util.List;

final class PercentileCalculator {

  private PercentileCalculator() {}

  static long compute(List<Long> sortedAsc, int percentile) {
    if (sortedAsc.isEmpty()) return 0L;
    int index = (int) Math.ceil(percentile / 100.0 * sortedAsc.size()) - 1;
    return sortedAsc.get(Math.max(0, index));
  }
}
