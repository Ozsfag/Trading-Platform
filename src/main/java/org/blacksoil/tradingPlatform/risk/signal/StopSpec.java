package org.blacksoil.tradingPlatform.risk.signal;

/** Stop spec in percent of entry price. Example: 0.02 = 2% stop distance. */
public record StopSpec(double stopDistancePct) {

  public StopSpec {
    if (!(stopDistancePct > 0.0 && stopDistancePct < 1.0)) {
      throw new IllegalArgumentException("stopDistancePct must be in (0,1)");
    }
  }
}
