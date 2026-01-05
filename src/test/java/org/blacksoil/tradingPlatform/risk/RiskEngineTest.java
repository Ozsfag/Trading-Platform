package org.blacksoil.tradingPlatform.risk;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.blacksoil.tradingPlatform.risk.app.DefaultRiskEngine;
import org.blacksoil.tradingPlatform.risk.app.RiskDecision;
import org.blacksoil.tradingPlatform.risk.app.RiskReason;
import org.blacksoil.tradingPlatform.risk.config.PerformanceStats;
import org.blacksoil.tradingPlatform.risk.config.RiskConfig;
import org.blacksoil.tradingPlatform.risk.signal.Signal;
import org.blacksoil.tradingPlatform.risk.signal.SignalType;
import org.blacksoil.tradingPlatform.risk.state.AccountState;
import org.blacksoil.tradingPlatform.risk.state.PositionState;
import org.junit.jupiter.api.Test;

class RiskEngineTest {

  private DefaultRiskEngine engine() {
    return new DefaultRiskEngine(RiskConfig.defaults());
  }

  private Signal buy() {
    return Signal.buy("BTC-USDT", "test");
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
    var signal = new Signal("BTC-USDT", SignalType.NO_SIGNAL, "none");

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
}
