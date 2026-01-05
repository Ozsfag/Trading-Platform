package org.blacksoil.tradingPlatform.risk.app.rules;

import java.util.Optional;
import org.blacksoil.tradingPlatform.risk.app.RiskContext;
import org.blacksoil.tradingPlatform.risk.app.RiskDecision;
import org.blacksoil.tradingPlatform.risk.app.RiskReason;
import org.blacksoil.tradingPlatform.risk.config.RiskConfig;

public final class OrdersPerDayRule implements RiskRule {

  private final RiskConfig cfg;

  public OrdersPerDayRule(RiskConfig cfg) {
    this.cfg = cfg;
  }

  @Override
  public Optional<RiskDecision> apply(RiskContext ctx) {
    if (ctx.account().ordersToday() >= cfg.ordersPerDay()) {
      return Optional.of(
          new RiskDecision.Block(
              RiskReason.ORDERS_PER_DAY,
              "Orders per day limit reached (" + cfg.ordersPerDay() + ")."));
    }
    return Optional.empty();
  }
}
