package org.blacksoil.tradingPlatform.risk.app.rules;

import java.time.Duration;
import java.util.Optional;
import org.blacksoil.tradingPlatform.risk.app.RiskContext;
import org.blacksoil.tradingPlatform.risk.app.RiskDecision;
import org.blacksoil.tradingPlatform.risk.app.RiskReason;
import org.blacksoil.tradingPlatform.risk.config.RiskConfig;

public final class KillSwitchRule implements RiskRule {

  private final RiskConfig cfg;

  public KillSwitchRule(RiskConfig cfg) {
    this.cfg = cfg;
  }

  @Override
  public Optional<RiskDecision> apply(RiskContext ctx) {
    if (ctx.stats().consecutiveLosses() >= cfg.maxConsecutiveLosses()) {
      var until = ctx.now().plus(Duration.ofDays(cfg.killSwitchPauseDays()));
      return Optional.of(
          new RiskDecision.Pause(
              RiskReason.KILL_SWITCH_MAX_CONSECUTIVE_LOSSES,
              "Kill-switch: max consecutive losses reached.",
              until));
    }
    return Optional.empty();
  }
}
