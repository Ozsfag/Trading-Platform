package org.blacksoil.tradingPlatform.risk.config;

public record RiskConfig(
    int maxOpenPositions,
    int ordersPerDay,
    double riskPerTradePct,
    double dailyLossLimitR,
    double weeklyLossLimitR,
    int maxConsecutiveLosses,
    int cooldownAfterLossDays,
    int killSwitchPauseDays) {
  public static RiskConfig defaults() {
    return new RiskConfig(1, 1, 0.005, -2.0, -5.0, 5, 1, 7);
  }
}
