package org.blacksoil.tradingPlatform;

import org.springframework.boot.SpringApplication;

public class TestTradingPlatformApplication {

  static void main(String[] args) {
    SpringApplication.from(TradingPlatformApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
