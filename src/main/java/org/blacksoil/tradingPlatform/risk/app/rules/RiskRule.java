package org.blacksoil.tradingPlatform.risk.app.rules;

import java.util.Optional;
import org.blacksoil.tradingPlatform.risk.app.RiskContext;
import org.blacksoil.tradingPlatform.risk.app.RiskDecision;

@FunctionalInterface
public interface RiskRule {
  Optional<RiskDecision> apply(RiskContext ctx);
}
