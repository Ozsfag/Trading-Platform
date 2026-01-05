package org.blacksoil.tradingPlatform.risk.util;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

public final class RiskTime {

  private RiskTime() {}

  public static Instant nextDayStart(Instant now, ZoneId zoneId) {
    var zdt = now.atZone(zoneId);
    var nextDayStart = zdt.toLocalDate().plusDays(1).atStartOfDay(zoneId);
    return nextDayStart.toInstant();
  }

  public static Instant nextWeekStart(Instant now, ZoneId zoneId) {
    // week starts Monday 00:00 in the given zone
    var zdt = now.atZone(zoneId);
    var nextMonday = zdt.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    return nextMonday.atStartOfDay(zoneId).toInstant();
  }
}
