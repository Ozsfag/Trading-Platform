package org.blacksoil.tradingPlatform.risk.util;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;

public final class RiskTime {
  private RiskTime() {}

  public static Instant nextDayStartUtc(Instant now) {
    var zdt = now.atZone(ZoneOffset.UTC);
    var nextDayStart = zdt.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC);
    return nextDayStart.toInstant();
  }

  public static Instant nextWeekStartUtc(Instant now) {
    // week starts Monday 00:00 UTC
    var zdt = now.atZone(ZoneOffset.UTC);
    var nextMonday = zdt.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    return nextMonday.atStartOfDay(ZoneOffset.UTC).toInstant();
  }
}
