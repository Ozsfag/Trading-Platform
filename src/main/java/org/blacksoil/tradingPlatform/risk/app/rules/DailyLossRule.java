package org.blacksoil.tradingPlatform.risk.app.rules;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;
import org.blacksoil.tradingPlatform.risk.app.RiskContext;
import org.blacksoil.tradingPlatform.risk.app.RiskDecision;
import org.blacksoil.tradingPlatform.risk.config.RiskConfig;

public final class DailyLossRule implements RiskRule {

  private final RiskConfig cfg;
  private final Function<Instant, Instant> nextDayStart;

  public DailyLossRule(RiskConfig cfg, Function<Instant, Instant> nextDayStart) {
    this.cfg = cfg;
    this.nextDayStart = nextDayStart;
  }

  @Override
  public Optional<RiskDecision> apply(RiskContext ctx) {
    if (ctx.stats().dailyR() <= cfg.dailyLossLimitR()) {
      var until = nextDayStart.apply(ctx.now());
      return Optional.of(
          new RiskDecision.Pause(RiskReason.DAILY_LOSS_LIMIT, "Daily loss limit reached.", until));
    }
    return Optional.empty();
  }
}
