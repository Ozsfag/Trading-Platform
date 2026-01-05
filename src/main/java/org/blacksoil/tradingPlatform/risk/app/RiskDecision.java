package org.blacksoil.tradingPlatform.risk.app;

import java.time.Instant;

public sealed interface RiskDecision {

  record Allow() implements RiskDecision {}

  record Block(RiskReason reason, String message) implements RiskDecision {}

  record Pause(RiskReason reason, String message, Instant until) implements RiskDecision {}
}
