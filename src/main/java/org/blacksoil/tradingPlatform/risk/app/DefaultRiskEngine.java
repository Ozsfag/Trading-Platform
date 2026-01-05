package org.blacksoil.tradingPlatform.risk.app;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.blacksoil.tradingPlatform.risk.app.rules.*;
import org.blacksoil.tradingPlatform.risk.config.PerformanceStats;
import org.blacksoil.tradingPlatform.risk.config.RiskConfig;
import org.blacksoil.tradingPlatform.risk.signal.Signal;
import org.blacksoil.tradingPlatform.risk.state.AccountState;
import org.blacksoil.tradingPlatform.risk.state.PositionState;
import org.blacksoil.tradingPlatform.risk.util.RiskTime;

public final class DefaultRiskEngine implements RiskEngine {

  private final List<RiskRule> rules;

  public DefaultRiskEngine(RiskConfig cfg) {
    this.rules =
        List.of(
            new LongOnlyRule(),
            new KillSwitchRule(cfg),
            new CooldownRule(),
            new DailyLossRule(cfg, RiskTime::nextDayStartUtc),
            new WeeklyLossRule(cfg, RiskTime::nextWeekStartUtc),
            new MaxOpenPositionsRule(cfg),
            new OrdersPerDayRule(cfg));
  }

  @Override
  public RiskDecision evaluate(
      Signal signal, AccountState acc, PositionState pos, PerformanceStats stats, Instant now) {
    var ctx = new RiskContext(signal, acc, pos, stats, now);

    Optional<RiskDecision> decision =
        rules.stream()
            .map(rule -> rule.apply(ctx))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

    return decision.orElseGet(RiskDecision.Allow::new);
  }
}
