package com.ss.utopia.auth.entity;

//todo get ttl from config
public enum AccountAction {
  PASSWORD_RESET(10), // 10 minutes
  CONFIRMATION(60); // 1 hour

  private int minutesToLive;

  AccountAction(int minutesToLive) {
    this.minutesToLive = minutesToLive;
  }

  public int getMinutesToLive() {
    return minutesToLive;
  }
}
