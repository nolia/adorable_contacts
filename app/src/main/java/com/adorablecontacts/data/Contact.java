package com.adorablecontacts.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Contact {

  private final String name;
  private final String phoneNumber;

  public Contact(@NonNull final String name, @Nullable final String phoneNumber) {
    this.name = name;
    this.phoneNumber = phoneNumber;
  }

  public String getName() {
    return name;
  }

  @Nullable
  public String getPhoneNumber() {
    return phoneNumber;
  }


}
