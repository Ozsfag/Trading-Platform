package org.blacksoil.tradingPlatform.risk.app;

public record PositionSizing(double equity, double riskPct, double riskAmount) {

  public static PositionSizing of(double equity, double riskPct) {
    return new PositionSizing(equity, riskPct, equity * riskPct);
  }
}
