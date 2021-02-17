package com.ss.utopia.auth.entity;

public enum UserRole {
  DEFAULT("DEFAULT"),
  CUSTOMER("CUSTOMER"),
  TRAVEL_AGENT("TRAVEL_AGENT"),
  EMPLOYEE("EMPLOYEE"),
  ADMIN("ADMIN");

  private final String roleName;

  UserRole(String roleName) {
    this.roleName = roleName;
  }

  public String getRoleName() {
    return roleName;
  }
}