package org.blacksoil.tradingPlatform.risk.app;

import java.time.Instant;
import org.blacksoil.tradingPlatform.risk.signal.StopSpec;

public sealed interface RiskDecision {

  record Allow(PositionSizing sizing, StopSpec stopSpec) implements RiskDecision {}

  record Block(RiskReason reason, String message) implements RiskDecision {}

  record Pause(RiskReason reason, String message, Instant until) implements RiskDecision {}
}
