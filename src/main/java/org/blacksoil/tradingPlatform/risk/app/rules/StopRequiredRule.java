package org.blacksoil.tradingPlatform.risk.app.rules;

import java.util.Optional;
import org.blacksoil.tradingPlatform.risk.app.RiskContext;
import org.blacksoil.tradingPlatform.risk.app.RiskDecision;
import org.blacksoil.tradingPlatform.risk.app.RiskReason;

public final class StopRequiredRule implements RiskRule {

  @Override
  public Optional<RiskDecision> apply(RiskContext ctx) {
    // Only relevant for BUY signals
    if (ctx.signal().isBuy() && !ctx.signal().hasStop()) {
      return Optional.of(
          new RiskDecision.Block(RiskReason.STOP_REQUIRED, "Stop is required for every trade."));
    }
    return Optional.empty();
  }
}
