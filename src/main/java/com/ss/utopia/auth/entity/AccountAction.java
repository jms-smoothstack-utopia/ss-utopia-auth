package com.ss.utopia.auth.entity;

public enum AccountAction {
  PASSWORD_RESET(10), // 10 minutes
  CONFIRMATION(60), // 1 hour
  DELETION(10); // 10 minutes

  private final int minutesToLive;

  AccountAction(int minutesToLive) {
    this.minutesToLive = minutesToLive;
  }

  public int getMinutesToLive() {
    return minutesToLive;
  }
}
