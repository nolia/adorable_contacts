package com.adorablecontacts.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class ContactProvider {

  private static final String[] PROJECTION = new String[]{
      Contacts.LOOKUP_KEY,
      Contacts.DISPLAY_NAME,
      Contacts.HAS_PHONE_NUMBER
  };
  private static final String[] PROJECTION_PHONE = new String[]{
      Phone.NORMALIZED_NUMBER
  };
  private static final String CONTACTS_SORT_ORDER = Contacts.DISPLAY_NAME + " ASC";

  private final Context context;

  public ContactProvider(final Context context) {
    this.context = context;
  }

  public Observable<List<Contact>> getContacts() {
    return Observable.defer(() -> {
      Cursor contactsCursor = null;
      try {
        final List<Contact> contacts = new ArrayList<>();

        contactsCursor = context.getContentResolver()
            .query(
                Contacts.CONTENT_URI,
                PROJECTION,
                null,
                null,
                CONTACTS_SORT_ORDER
            );


        if (contactsCursor == null) {
          return Observable.just(contacts);
        }
        // Old-style way: cache the column indexes, as they potentially might change.
        final int lookupKeyCol = contactsCursor.getColumnIndex(Contacts.LOOKUP_KEY);
        final int displayNameCol = contactsCursor.getColumnIndex(Contacts.DISPLAY_NAME);
        final int hasPhoneNumberCol = contactsCursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER);

        for (int pos = 0; pos < contactsCursor.getCount(); pos++) {
          contactsCursor.moveToPosition(pos);
          final String name = contactsCursor.getString(displayNameCol);
          String phoneNumber = null;
          if (contactsCursor.getInt(hasPhoneNumberCol) > 0) {
            phoneNumber = getPhoneNumberFor(contactsCursor.getString(lookupKeyCol));
          }
          contacts.add(new Contact(name, phoneNumber));
        }

        return Observable.just(contacts);
      } catch (RuntimeException e) {
        throw new RuntimeException(e);
      } finally {
        if (contactsCursor != null) {
          contactsCursor.close();
        }
      }

    })
        .subscribeOn(Schedulers.io());
  }

  private String getPhoneNumberFor(final String lookupKey) {
    String phoneNumber = null;
    Cursor phonesCursor = null;
    try {
      phonesCursor = context.getContentResolver()
          .query(
              Phone.CONTENT_URI,
              PROJECTION_PHONE,
              Phone.LOOKUP_KEY + " = ? AND " + Phone.NUMBER + " != \"\"",
              new String[]{lookupKey},
              Phone.IS_PRIMARY + " DESC" // Get primary phones first.
          );

      if (phonesCursor != null && phonesCursor.moveToFirst()) {
        phoneNumber = phonesCursor.getString(0); // As we getting only normalized phone number.
      }

    } finally {
      if (phonesCursor != null) {
        phonesCursor.close();
      }
    }

    return phoneNumber;
  }
}
