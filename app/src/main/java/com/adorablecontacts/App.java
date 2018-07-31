package com.adorablecontacts;

import android.app.Application;

import com.adorablecontacts.data.AvatarProvider;
import com.adorablecontacts.data.ContactProvider;

public class App extends Application {

  private AvatarProvider avatarProvider;
  private ContactProvider contactProvider;

  @Override
  public void onCreate() {
    super.onCreate();

    avatarProvider = new AvatarProvider(this);
    contactProvider = new ContactProvider(this);
  }

  public AvatarProvider getAvatarProvider() {
    return avatarProvider;
  }

  public ContactProvider getContactProvider() {
    return contactProvider;
  }
}
