package com.adorablecontacts.data;

public class ContactDetailsItem {

  private final String data;
  private final String info;

  public ContactDetailsItem(final String data, final String info) {
    this.data = data;
    this.info = info;
  }

  public String getData() {
    return data;
  }

  public String getInfo() {
    return info;
  }
}
