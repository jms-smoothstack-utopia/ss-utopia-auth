package com.ss.utopia.auth.entity;

public enum UserRole {
  DEFAULT("ROLE_DEFAULT"),
  CUSTOMER("ROLE_CUSTOMER"),
  TRAVEL_AGENT("ROLE_TRAVEL_AGENT"),
  EMPLOYEE("ROLE_EMPLOYEE"),
  ADMIN("ROLE_ADMIN");

  private final String roleName;

  UserRole(String roleName) {
    this.roleName = roleName;
  }

  public String getRole() {
    return roleName;
  }

  public String getRoleName() {
    return roleName.replace("ROLE_", "");
  }
}