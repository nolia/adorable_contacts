package com.adorablecontacts.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Contact implements Parcelable {

  private final String lookupKey;
  private final String name;
  private final String phoneNumber;

  public Contact(final String lookupKey,
                 @NonNull final String name, @Nullable final String phoneNumber) {
    this.lookupKey = lookupKey;
    this.name = name;
    this.phoneNumber = phoneNumber;
  }

  protected Contact(Parcel in) {
    this.lookupKey = in.readString();
    this.name = in.readString();
    this.phoneNumber = in.readString();
  }

  public String getLookupKey() {
    return lookupKey;
  }

  public String getName() {
    return name;
  }

  @Nullable
  public String getPhoneNumber() {
    return phoneNumber;
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.lookupKey);
    dest.writeString(this.name);
    dest.writeString(this.phoneNumber);
  }

  public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
    @Override
    public Contact createFromParcel(Parcel source) {
      return new Contact(source);
    }

    @Override
    public Contact[] newArray(int size) {
      return new Contact[size];
    }
  };
}
