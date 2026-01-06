package org.blacksoil.tradingPlatform.risk;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.blacksoil.tradingPlatform.risk.app.DefaultRiskEngine;
import org.blacksoil.tradingPlatform.risk.app.RiskDecision;
import org.blacksoil.tradingPlatform.risk.app.RiskReason;
import org.blacksoil.tradingPlatform.risk.config.PerformanceStats;
import org.blacksoil.tradingPlatform.risk.config.RiskConfig;
import org.blacksoil.tradingPlatform.risk.signal.Signal;
import org.blacksoil.tradingPlatform.risk.signal.SignalType;
import org.blacksoil.tradingPlatform.risk.signal.StopSpec;
import org.blacksoil.tradingPlatform.risk.state.AccountState;
import org.blacksoil.tradingPlatform.risk.state.PositionState;
import org.junit.jupiter.api.Test;

class RiskEngineTest {

  private DefaultRiskEngine engine() {
    return new DefaultRiskEngine(RiskConfig.defaults());
  }

  private Signal buy() {
    return Signal.buy("BTC-USDT", "test", new StopSpec(0.02));
  }

  private AccountState acc() {
    return AccountState.withEquity(10_000.0);
  }

  private PositionState noPos() {
    return PositionState.none();
  }

  @Test
  void blocksWhenPositionAlreadyOpen() {
    var decision =
        engine()
            .evaluate(
                buy(),
                acc(),
                PositionState.open("BTC-USDT"),
                PerformanceStats.empty(),
                Instant.parse("2026-01-05T10:00:00Z"));

    assertThat(decision).isInstanceOf(RiskDecision.Block.class);
    assertThat(((RiskDecision.Block) decision).reason()).isEqualTo(RiskReason.MAX_OPEN_POSITIONS);
  }

  @Test
  void blocksWhenOrdersPerDayLimitReached() {
    var decision =
        engine()
            .evaluate(
                buy(),
                AccountState.withEquityAndOrdersToday(10_000.0, 1),
                noPos(),
                PerformanceStats.empty(),
                Instant.parse("2026-01-05T10:00:00Z"));

    assertThat(decision).isInstanceOf(RiskDecision.Block.class);
    assertThat(((RiskDecision.Block) decision).reason()).isEqualTo(RiskReason.ORDERS_PER_DAY);
  }

  @Test
  void pausesWhenCooldownIsActive() {
    var cooldownUntil = Instant.parse("2026-01-06T00:00:00Z");

    var decision =
        engine()
            .evaluate(
                buy(),
                acc(),
                noPos(),
                new PerformanceStats(0, 0.0, 0.0, cooldownUntil),
                Instant.parse("2026-01-05T10:00:00Z"));

    assertThat(decision).isInstanceOf(RiskDecision.Pause.class);
    var pause = (RiskDecision.Pause) decision;

    assertThat(pause.reason()).isEqualTo(RiskReason.COOLDOWN_ACTIVE);
    assertThat(pause.until()).isEqualTo(cooldownUntil);
  }

  @Test
  void killSwitchPausesWhenConsecutiveLossesReached() {
    var now = Instant.parse("2026-01-05T10:00:00Z");

    var decision =
        engine().evaluate(buy(), acc(), noPos(), new PerformanceStats(5, 0.0, 0.0, null), now);

    assertThat(decision).isInstanceOf(RiskDecision.Pause.class);
    var pause = (RiskDecision.Pause) decision;

    assertThat(pause.reason()).isEqualTo(RiskReason.KILL_SWITCH_MAX_CONSECUTIVE_LOSSES);
    assertThat(pause.until()).isEqualTo(now.plus(Duration.ofDays(7)));
  }

  @Test
  void pausesUntilNextDayStartWhenDailyLossLimitBreached() {
    var now = Instant.parse("2026-01-05T10:15:00Z");

    var decision =
        engine().evaluate(buy(), acc(), noPos(), new PerformanceStats(0, -2.0, 0.0, null), now);

    assertThat(decision).isInstanceOf(RiskDecision.Pause.class);
    var pause = (RiskDecision.Pause) decision;

    assertThat(pause.reason()).isEqualTo(RiskReason.DAILY_LOSS_LIMIT);
    assertThat(pause.until()).isEqualTo(Instant.parse("2026-01-06T00:00:00Z"));
  }

  @Test
  void pausesUntilNextWeekStartWhenWeeklyLossLimitBreached() {
    var now = Instant.parse("2026-01-05T10:15:00Z"); // Monday

    var decision =
        engine().evaluate(buy(), acc(), noPos(), new PerformanceStats(0, 0.0, -5.0, null), now);

    assertThat(decision).isInstanceOf(RiskDecision.Pause.class);
    var pause = (RiskDecision.Pause) decision;

    assertThat(pause.reason()).isEqualTo(RiskReason.WEEKLY_LOSS_LIMIT);
    assertThat(pause.until()).isEqualTo(Instant.parse("2026-01-12T00:00:00Z"));
  }

  @Test
  void priority_killSwitchBeatsOrdersPerDay() {
    var now = Instant.parse("2026-01-05T10:00:00Z");

    var decision =
        engine()
            .evaluate(
                buy(),
                AccountState.withEquityAndOrdersToday(10_000.0, 1),
                noPos(),
                new PerformanceStats(5, 0.0, 0.0, null),
                now);

    assertThat(decision).isInstanceOf(RiskDecision.Pause.class);
    assertThat(((RiskDecision.Pause) decision).reason())
        .isEqualTo(RiskReason.KILL_SWITCH_MAX_CONSECUTIVE_LOSSES);
  }

  @Test
  void blocksWhenSignalIsNotBuy() {
    var signal = new Signal("BTC-USDT", SignalType.NO_SIGNAL, "none", new StopSpec(0.02));

    var decision =
        engine()
            .evaluate(
                signal,
                acc(),
                noPos(),
                PerformanceStats.empty(),
                Instant.parse("2026-01-05T10:00:00Z"));

    assertThat(decision).isInstanceOf(RiskDecision.Block.class);
    assertThat(((RiskDecision.Block) decision).reason()).isEqualTo(RiskReason.NOT_BUY_LONG_ONLY);
  }

  @Test
  void returnsAllowWithPositionSizing() {
    var decision =
        engine()
            .evaluate(
                buy(),
                AccountState.withEquity(10_000.0),
                noPos(),
                PerformanceStats.empty(),
                Instant.parse("2026-01-05T10:00:00Z"));

    assertThat(decision).isInstanceOf(RiskDecision.Allow.class);

    var allow = (RiskDecision.Allow) decision;
    assertThat(allow.sizing().equity()).isEqualTo(10_000.0);
    assertThat(allow.sizing().riskPct()).isEqualTo(0.005);
    assertThat(allow.sizing().riskAmount()).isEqualTo(50.0);

    assertThat(allow.stopSpec().stopDistancePct()).isEqualTo(0.02);
  }

  @Test
  void dailyLossPauseRespectsZoneId() {
    var cfg = new RiskConfig(1, 1, 0.005, -2.0, -5.0, 5, 1, 7, ZoneId.of("America/New_York"));

    var engine = new DefaultRiskEngine(cfg);

    // 2026-01-05T23:00:00Z == 18:00 in New York (winter time, UTC-5)
    var now = Instant.parse("2026-01-05T23:00:00Z");

    var decision =
        engine.evaluate(buy(), acc(), noPos(), new PerformanceStats(0, -2.0, 0.0, null), now);

    assertThat(decision).isInstanceOf(RiskDecision.Pause.class);
    var pause = (RiskDecision.Pause) decision;

    // Next day start in New York: 2026-01-06 00:00 NY => 2026-01-06T05:00:00Z
    assertThat(pause.reason()).isEqualTo(RiskReason.DAILY_LOSS_LIMIT);
    assertThat(pause.until()).isEqualTo(Instant.parse("2026-01-06T05:00:00Z"));
  }

  @Test
  void blocksWhenStopIsMissing() {
    var signal = Signal.buy("BTC-USDT", "test"); // stopSpec = null

    var decision =
        engine()
            .evaluate(
                signal,
                acc(),
                noPos(),
                PerformanceStats.empty(),
                Instant.parse("2026-01-05T10:00:00Z"));

    assertThat(decision).isInstanceOf(RiskDecision.Block.class);
    assertThat(((RiskDecision.Block) decision).reason()).isEqualTo(RiskReason.STOP_REQUIRED);
  }
}
