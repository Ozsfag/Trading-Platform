package org.blacksoil.tradingPlatform.risk;

import static org.assertj.core.api.Assertions.*;

import org.blacksoil.tradingPlatform.risk.app.PositionSizing;
import org.junit.jupiter.api.Test;

class PositionSizingTest {

  @Test
  void quantityIsRiskAmountDividedByStopDistance() {
    // equity=10_000, riskPct=0.5% => riskAmount=50
    var sizing = PositionSizing.of(10_000.0, 0.005);

    // entry=100, stopDistancePct=2% => stopDistance=2
    // qty = 50 / 2 = 25
    var qty = sizing.quantityForStopDistancePct(100.0, 0.02);

    assertThat(qty).isEqualTo(25.0);
  }

  @Test
  void throwsWhenEntryPriceIsNotPositive() {
    var sizing = PositionSizing.of(10_000.0, 0.005);

    assertThatThrownBy(() -> sizing.quantityForStopDistancePct(0.0, 0.02))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void throwsWhenStopDistancePctIsInvalid() {
    var sizing = PositionSizing.of(10_000.0, 0.005);

    assertThatThrownBy(() -> sizing.quantityForStopDistancePct(100.0, 0.0))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> sizing.quantityForStopDistancePct(100.0, 1.0))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
