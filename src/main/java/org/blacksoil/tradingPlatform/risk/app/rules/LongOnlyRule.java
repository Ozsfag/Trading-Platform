package org.blacksoil.tradingPlatform.risk.app.rules;

import java.util.Optional;
import org.blacksoil.tradingPlatform.risk.app.RiskContext;
import org.blacksoil.tradingPlatform.risk.app.RiskDecision;
import org.blacksoil.tradingPlatform.risk.app.RiskReason;

public final class LongOnlyRule implements RiskRule {

  @Override
  public Optional<RiskDecision> apply(RiskContext ctx) {
    if (!ctx.signal().isBuy()) {
      return Optional.of(
          new RiskDecision.Block(
              RiskReason.NOT_BUY_LONG_ONLY, "Only BUY signals are supported (spot, long-only)."));
    }
    return Optional.empty();
  }
}
