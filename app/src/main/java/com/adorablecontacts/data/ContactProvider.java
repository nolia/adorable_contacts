package com.adorablecontacts.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

import com.adorablecontacts.R;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static android.provider.ContactsContract.Data;

@EBean(scope = EBean.Scope.Singleton)
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

  private static final String[] DETAILS_PROJECTION = {
      Data.MIMETYPE,
      Data.DATA1
  };
  private static final String ORDER_PRIMARY_FIRST = Phone.IS_PRIMARY + " DESC";

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
          final String lookupKey = contactsCursor.getString(lookupKeyCol);
          final String name = contactsCursor.getString(displayNameCol);
          String phoneNumber = null;
          if (contactsCursor.getInt(hasPhoneNumberCol) > 0) {

            phoneNumber = getPhoneNumberFor(lookupKey);
          }
          contacts.add(new Contact(lookupKey, name, phoneNumber));
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

  public Observable<List<ContactDetailsItem>> getContactDetails(final Contact contact) {
    return Observable.defer(() -> {
      final List<ContactDetailsItem> detailsItemList = new ArrayList<>();

      final Cursor dataCursor = context.getContentResolver().query(
          Data.CONTENT_URI,
          DETAILS_PROJECTION,
          Data.LOOKUP_KEY + " = ?",
          new String[]{contact.getLookupKey()},
          Data.IS_PRIMARY + " DESC"

      );
      if (dataCursor != null) {
        final int mimeTypeCol = dataCursor.getColumnIndex(Data.MIMETYPE);
        final int dataCol = dataCursor.getColumnIndex(Data.DATA1);

        for (int i = 0; i < dataCursor.getCount(); i++) {
          dataCursor.moveToPosition(i);

          final String data = dataCursor.getString(dataCol);
          final String info = getInfoFor(dataCursor.getString(mimeTypeCol));
          detailsItemList.add(new ContactDetailsItem(data, info));
        }
        dataCursor.close();
      }

      return Observable.just(detailsItemList);
    }).subscribeOn(Schedulers.io());
  }

  private String getInfoFor(final String mimeType) {
    final int resId;
    switch (mimeType) {
      case CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
        resId = R.string.label_name;
        break;
      case CommonDataKinds.Nickname.CONTENT_ITEM_TYPE:
        resId = R.string.label_nickname;
        break;
      case CommonDataKinds.Email.CONTENT_ITEM_TYPE:
        resId = R.string.label_email;
        break;
      case CommonDataKinds.Website.CONTENT_ITEM_TYPE:
        resId = R.string.label_website;
        break;
      case CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE:
        resId = R.string.label_postal_address;
        break;
      case Phone.CONTENT_ITEM_TYPE:
        resId = R.string.label_phone;
        break;
      default:
        resId = R.string.label_other;
    }
    return context.getString(resId);
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
              ORDER_PRIMARY_FIRST
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
