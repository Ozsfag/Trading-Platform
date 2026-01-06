package org.blacksoil.tradingPlatform.risk.signal;

public record Signal(String symbol, SignalType type, String reason, StopSpec stopSpec) {

  public static Signal buy(String symbol, String reason, StopSpec stopSpec) {
    return new Signal(symbol, SignalType.BUY, reason, stopSpec);
  }

  public static Signal buy(String symbol, String reason) {
    // backward compatible factory for existing tests/calls (will now fail risk rule if stop is
    // required)
    return new Signal(symbol, SignalType.BUY, reason, null);
  }

  public boolean isBuy() {
    return type == SignalType.BUY;
  }

  public boolean hasStop() {
    return stopSpec != null;
  }
}
