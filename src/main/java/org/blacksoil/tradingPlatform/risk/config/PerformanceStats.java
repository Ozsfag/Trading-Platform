package org.blacksoil.tradingPlatform.risk.config;

import java.time.Instant;

public record PerformanceStats(
    int consecutiveLosses, double dailyR, double weeklyR, Instant cooldownUntil) {
  public static PerformanceStats empty() {
    return new PerformanceStats(0, 0.0, 0.0, null);
  }
}
