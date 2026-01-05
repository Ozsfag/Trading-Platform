package org.blacksoil.tradingPlatform.risk.signal;

public record Signal(String symbol, SignalType type, String reason) {
  public static Signal buy(String symbol, String reason) {
    return new Signal(symbol, SignalType.BUY, reason);
  }

  public boolean isBuy() {
    return type == SignalType.BUY;
  }
}
