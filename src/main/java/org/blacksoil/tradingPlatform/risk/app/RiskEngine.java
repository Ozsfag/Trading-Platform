package org.blacksoil.tradingPlatform.risk.app;

import java.time.Instant;
import org.blacksoil.tradingPlatform.risk.config.PerformanceStats;
import org.blacksoil.tradingPlatform.risk.signal.Signal;
import org.blacksoil.tradingPlatform.risk.state.AccountState;
import org.blacksoil.tradingPlatform.risk.state.PositionState;

public interface RiskEngine {
  RiskDecision evaluate(
      Signal signal, AccountState acc, PositionState pos, PerformanceStats stats, Instant now);
}
