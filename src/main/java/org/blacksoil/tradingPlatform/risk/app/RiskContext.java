package org.blacksoil.tradingPlatform.risk.app;

import java.time.Instant;
import org.blacksoil.tradingPlatform.risk.config.PerformanceStats;
import org.blacksoil.tradingPlatform.risk.signal.Signal;
import org.blacksoil.tradingPlatform.risk.state.AccountState;
import org.blacksoil.tradingPlatform.risk.state.PositionState;

public record RiskContext(
    Signal signal,
    AccountState account,
    PositionState position,
    PerformanceStats stats,
    Instant now) {}
