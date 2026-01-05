package org.blacksoil.tradingPlatform.risk;

import org.blacksoil.tradingPlatform.risk.config.PerformanceStats;

import java.time.Instant;

public interface RiskEngine {
  RiskDecision evaluate(
          Signal signal, AccountState acc, PositionState pos, PerformanceStats stats, Instant now);
}
