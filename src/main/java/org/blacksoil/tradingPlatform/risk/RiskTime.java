package org.blacksoil.tradingPlatform.risk;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;

final class RiskTime {
  private RiskTime() {}

  static Instant nextDayStartUtc(Instant now) {
    var zdt = now.atZone(ZoneOffset.UTC);
    var nextDayStart = zdt.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC);
    return nextDayStart.toInstant();
  }

  static Instant nextWeekStartUtc(Instant now) {
    // week starts Monday 00:00 UTC
    var zdt = now.atZone(ZoneOffset.UTC);
    var nextMonday = zdt.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    return nextMonday.atStartOfDay(ZoneOffset.UTC).toInstant();
  }
}
