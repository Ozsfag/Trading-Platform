package org.blacksoil.tradingPlatform.risk.state;

public record PositionState(boolean open, String symbol) {
  public static PositionState none() {
    return new PositionState(false, null);
  }

  public static PositionState open(String symbol) {
    return new PositionState(true, symbol);
  }

  public boolean hasOpenPosition() {
    return open;
  }
}
