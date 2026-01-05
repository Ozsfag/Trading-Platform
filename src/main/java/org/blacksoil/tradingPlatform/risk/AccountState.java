package org.blacksoil.tradingPlatform.risk;

public record AccountState(double equity, int ordersToday) {
  public static AccountState withEquity(double equity) {
    return new AccountState(equity, 0);
  }

  public static AccountState withEquityAndOrdersToday(double equity, int ordersToday) {
    return new AccountState(equity, ordersToday);
  }
}
