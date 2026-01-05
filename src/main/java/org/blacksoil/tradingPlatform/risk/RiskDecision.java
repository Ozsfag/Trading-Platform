package org.blacksoil.tradingPlatform.risk;

import java.time.Instant;

public sealed interface RiskDecision {
  record Allow() implements RiskDecision {}

  record Block(String reason) implements RiskDecision {}

  record Pause(String reason, Instant until) implements RiskDecision {}
}
