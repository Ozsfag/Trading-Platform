package org.blacksoil.tradingPlatform.risk.app.rules;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;
import org.blacksoil.tradingPlatform.risk.app.RiskContext;
import org.blacksoil.tradingPlatform.risk.app.RiskDecision;
import org.blacksoil.tradingPlatform.risk.config.RiskConfig;

public final class WeeklyLossRule implements RiskRule {

  private final RiskConfig cfg;
  private final Function<Instant, Instant> nextWeekStart;

  public WeeklyLossRule(RiskConfig cfg, Function<Instant, Instant> nextWeekStart) {
    this.cfg = cfg;
    this.nextWeekStart = nextWeekStart;
  }

  @Override
  public Optional<RiskDecision> apply(RiskContext ctx) {
    if (ctx.stats().weeklyR() <= cfg.weeklyLossLimitR()) {
      var until = nextWeekStart.apply(ctx.now());
      return Optional.of(
          new RiskDecision.Pause(
              RiskReason.WEEKLY_LOSS_LIMIT, "Weekly loss limit reached.", until));
    }
    return Optional.empty();
  }
}
