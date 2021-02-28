package com.ss.utopia.auth.exception;

import java.util.NoSuchElementException;
import java.util.UUID;

public class NoSuchAccountActionToken extends NoSuchElementException {

  public NoSuchAccountActionToken(UUID confirmationTokenId) {
    super("No AccountActionToken found with id=" + confirmationTokenId);
  }
}
