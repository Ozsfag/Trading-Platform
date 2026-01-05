package org.blacksoil.tradingPlatform.risk;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.blacksoil.tradingPlatform.risk.config.PerformanceStats;
import org.blacksoil.tradingPlatform.risk.config.RiskConfig;
import org.junit.jupiter.api.Test;

class RiskEngineTest {

    @Test
    void blocksWhenPositionAlreadyOpen() {
        var engine = new DefaultRiskEngine(RiskConfig.defaults());

        var signal = Signal.buy("BTC-USDT", "test");
        var acc = AccountState.withEquity(10_000.0);
        var pos = PositionState.open("BTC-USDT");
        var stats = PerformanceStats.empty();

        var decision = engine.evaluate(signal, acc, pos, stats, Instant.parse("2026-01-05T10:00:00Z"));

        assertThat(decision).isInstanceOf(RiskDecision.Block.class);
        assertThat(((RiskDecision.Block) decision).reason()).containsIgnoringCase("max open positions");
    }

    @Test
    void blocksWhenOrdersPerDayLimitReached() {
        var engine = new DefaultRiskEngine(RiskConfig.defaults());

        var signal = Signal.buy("BTC-USDT", "test");
        var acc = AccountState.withEquityAndOrdersToday(10_000.0, 1); // лимит = 1
        var pos = PositionState.none();
        var stats = PerformanceStats.empty();

        var decision = engine.evaluate(signal, acc, pos, stats, Instant.parse("2026-01-05T10:00:00Z"));

        assertThat(decision).isInstanceOf(RiskDecision.Block.class);
        assertThat(((RiskDecision.Block) decision).reason()).containsIgnoringCase("orders per day");
    }
    @Test
    void pausesWhenCooldownIsActive() {
        var engine = new DefaultRiskEngine(RiskConfig.defaults());

        var signal = Signal.buy("BTC-USDT", "test");
        var acc = AccountState.withEquity(10_000.0);
        var pos = PositionState.none();

        var cooldownUntil = Instant.parse("2026-01-06T00:00:00Z");
        var stats = new PerformanceStats(0, 0.0, 0.0, cooldownUntil);

        var now = Instant.parse("2026-01-05T10:00:00Z");
        var decision = engine.evaluate(signal, acc, pos, stats, now);

        assertThat(decision).isInstanceOf(RiskDecision.Pause.class);
        var pause = (RiskDecision.Pause) decision;

        assertThat(pause.reason()).containsIgnoringCase("cooldown");
        assertThat(pause.until()).isEqualTo(cooldownUntil);
    }

    @Test
    void killSwitchPausesWhenConsecutiveLossesReached() {
        var engine = new DefaultRiskEngine(RiskConfig.defaults());

        var signal = Signal.buy("BTC-USDT", "test");
        var acc = AccountState.withEquity(10_000.0);
        var pos = PositionState.none();

        var stats = new PerformanceStats(5, 0.0, 0.0, null); // достигли лимита
        var now = Instant.parse("2026-01-05T10:00:00Z");

        var decision = engine.evaluate(signal, acc, pos, stats, now);

        assertThat(decision).isInstanceOf(RiskDecision.Pause.class);
        var pause = (RiskDecision.Pause) decision;

        assertThat(pause.reason()).containsIgnoringCase("kill");
        assertThat(pause.until()).isEqualTo(now.plus(Duration.ofDays(7)));
    }

    @Test
    void pausesUntilNextDayStartWhenDailyLossLimitBreached() {
        var engine = new DefaultRiskEngine(RiskConfig.defaults());

        var signal = Signal.buy("BTC-USDT", "test");
        var acc = AccountState.withEquity(10_000.0);
        var pos = PositionState.none();

        // daily loss <= -2R triggers pause until next day start (UTC)
        var stats = new PerformanceStats(0, -2.0, 0.0, null);
        var now = Instant.parse("2026-01-05T10:15:00Z");

        var decision = engine.evaluate(signal, acc, pos, stats, now);

        assertThat(decision).isInstanceOf(RiskDecision.Pause.class);
        var pause = (RiskDecision.Pause) decision;

        assertThat(pause.reason()).containsIgnoringCase("daily loss");
        assertThat(pause.until()).isEqualTo(Instant.parse("2026-01-06T00:00:00Z"));
    }

    @Test
    void pausesUntilNextWeekStartWhenWeeklyLossLimitBreached() {
        var engine = new DefaultRiskEngine(RiskConfig.defaults());

        var signal = Signal.buy("BTC-USDT", "test");
        var acc = AccountState.withEquity(10_000.0);
        var pos = PositionState.none();

        // weekly loss <= -5R triggers pause until next week start (UTC, week starts Monday)
        var stats = new PerformanceStats(0, 0.0, -5.0, null);
        var now = Instant.parse("2026-01-05T10:15:00Z"); // Monday

        var decision = engine.evaluate(signal, acc, pos, stats, now);

        assertThat(decision).isInstanceOf(RiskDecision.Pause.class);
        var pause = (RiskDecision.Pause) decision;

        assertThat(pause.reason()).containsIgnoringCase("weekly loss");
        assertThat(pause.until()).isEqualTo(Instant.parse("2026-01-12T00:00:00Z")); // next Monday 00:00 UTC
    }


}
