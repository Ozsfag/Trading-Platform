package org.blacksoil.tradingPlatform.risk.app.rules;

import java.util.Optional;
import org.blacksoil.tradingPlatform.risk.app.RiskContext;
import org.blacksoil.tradingPlatform.risk.app.RiskDecision;
import org.blacksoil.tradingPlatform.risk.app.RiskReason;

public final class CooldownRule implements RiskRule {

  @Override
  public Optional<RiskDecision> apply(RiskContext ctx) {
    if (ctx.stats().cooldownUntil() != null && ctx.now().isBefore(ctx.stats().cooldownUntil())) {
      return Optional.of(
          new RiskDecision.Pause(
              RiskReason.COOLDOWN_ACTIVE,
              "Cooldown after loss is active.",
              ctx.stats().cooldownUntil()));
    }
    return Optional.empty();
  }
}
