package com.jetbrains.crucible.model;

import org.jetbrains.annotations.NotNull;

/**
 * User : ktisha
 * User abstraction.
 */
public class User {
  @NotNull
  protected final String myUserName;

  public User(@NotNull final String userName) {
    myUserName = userName;
  }

  @NotNull
  public String getUserName() {
    return myUserName;
  }

  @Override
  public String toString() {
    return "User [[" + myUserName+ "]]";
  }
}