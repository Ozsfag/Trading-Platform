package org.blacksoil.tradingPlatform.risk;

import java.time.Instant;
import org.blacksoil.tradingPlatform.risk.config.PerformanceStats;
import org.blacksoil.tradingPlatform.risk.config.RiskConfig;

public final class DefaultRiskEngine implements RiskEngine {
  private final RiskConfig cfg;

  public DefaultRiskEngine(RiskConfig cfg) {
    this.cfg = cfg;
  }

  @Override
  public RiskDecision evaluate(Signal signal, AccountState acc, PositionState pos, PerformanceStats stats, Instant now) {

    // spot, long-only guard
    if (!signal.isBuy()) {
      return new RiskDecision.Block("Only BUY signals are supported (spot, long-only).");
    }

    // kill-switch: max consecutive losses
    if (stats.consecutiveLosses() >= cfg.maxConsecutiveLosses()) {
      var until = now.plus(java.time.Duration.ofDays(cfg.killSwitchPauseDays()));
      return new RiskDecision.Pause("Kill-switch: max consecutive losses reached.", until);
    }

    // cooldown after loss
    if (stats.cooldownUntil() != null && now.isBefore(stats.cooldownUntil())) {
      return new RiskDecision.Pause("Cooldown after loss is active.", stats.cooldownUntil());
    }

    // daily loss limit (-2R)
    if (stats.dailyR() <= cfg.dailyLossLimitR()) {
      var until = RiskTime.nextDayStartUtc(now);
      return new RiskDecision.Pause("Daily loss limit reached.", until);
    }

    // weekly loss limit (-5R)
    if (stats.weeklyR() <= cfg.weeklyLossLimitR()) {
      var until = RiskTime.nextWeekStartUtc(now);
      return new RiskDecision.Pause("Weekly loss limit reached.", until);
    }

    // max 1 open position
    if (pos.hasOpenPosition()) {
      return new RiskDecision.Block("Max open positions reached (" + cfg.maxOpenPositions() + ").");
    }

    // 1 order per day (training stage)
    if (acc.ordersToday() >= cfg.ordersPerDay()) {
      return new RiskDecision.Block("Orders per day limit reached (" + cfg.ordersPerDay() + ").");
    }

    return new RiskDecision.Allow();
  }

}
