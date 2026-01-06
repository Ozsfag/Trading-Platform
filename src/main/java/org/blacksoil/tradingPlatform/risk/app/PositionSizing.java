package org.blacksoil.tradingPlatform.risk.app;

public record PositionSizing(double equity, double riskPct, double riskAmount) {

  public static PositionSizing of(double equity, double riskPct) {
    return new PositionSizing(equity, riskPct, equity * riskPct);
  }

  /**
   * @param entryPrice positive price
   * @param stopDistancePct (0,1) e.g. 0.02 = 2%
   * @return quantity so that monetary risk equals riskAmount
   */
  public double quantityForStopDistancePct(double entryPrice, double stopDistancePct) {
    if (entryPrice <= 0.0) {
      throw new IllegalArgumentException("entryPrice must be positive");
    }
    if (!(stopDistancePct > 0.0 && stopDistancePct < 1.0)) {
      throw new IllegalArgumentException("stopDistancePct must be in (0,1)");
    }

    double stopDistance = entryPrice * stopDistancePct;
    if (stopDistance <= 0.0) {
      throw new IllegalArgumentException("stopDistance must be positive");
    }

    return riskAmount / stopDistance;
  }
}
