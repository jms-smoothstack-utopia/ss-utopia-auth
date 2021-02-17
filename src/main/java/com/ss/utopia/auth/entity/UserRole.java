package com.ss.utopia.auth.entity;

public enum UserRole {
  DEFAULT("DEFAULT");

  private final String roleName;

  UserRole(String roleName) {
    this.roleName = roleName;
  }

  public String getRoleName() {
    return roleName;
  }
}