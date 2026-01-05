package org.blacksoil.tradingPlatform.risk.app.rules;

import java.util.Optional;
import org.blacksoil.tradingPlatform.risk.app.RiskContext;
import org.blacksoil.tradingPlatform.risk.app.RiskDecision;
import org.blacksoil.tradingPlatform.risk.config.RiskConfig;

public final class MaxOpenPositionsRule implements RiskRule {

  private final RiskConfig cfg;

  public MaxOpenPositionsRule(RiskConfig cfg) {
    this.cfg = cfg;
  }

  @Override
  public Optional<RiskDecision> apply(RiskContext ctx) {
    if (ctx.position().hasOpenPosition()) {
      return Optional.of(
          new RiskDecision.Block(
              RiskReason.MAX_OPEN_POSITIONS,
              "Max open positions reached (" + cfg.maxOpenPositions() + ")."));
    }
    return Optional.empty();
  }
}
