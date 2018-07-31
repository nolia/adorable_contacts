package com.adorablecontacts.data

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts

private val PROJECTION = arrayOf(
    Contacts._ID,
    Contacts.LOOKUP_KEY,
    Contacts.DISPLAY_NAME,
    Contacts.HAS_PHONE_NUMBER
)
private val PROJECTION_PHONE = arrayOf(
    Phone.LOOKUP_KEY,
    Phone.DISPLAY_NAME,
    Phone.NUMBER,
    Phone.NORMALIZED_NUMBER,
    Phone.IS_PRIMARY
)

data class Contact(
    val name: String,
    val phoneNumber: String? = null
)

class ContactsProvider(private val context: Context) {

  private fun loadContacts() =
      context.contentResolver.query(Contacts.CONTENT_URI,
          PROJECTION,
          null,
          null,
          "${Contacts.DISPLAY_NAME} ASC, ${Contacts.LOOKUP_KEY} ASC"
      )

  private inline val Cursor.lookupKey: String
    get() =
      this.getString(this.getColumnIndex(Contacts.LOOKUP_KEY))

  private inline val Cursor.displayName: String
    get() =
      this.getString(this.getColumnIndex(Contacts.DISPLAY_NAME))

  private inline val Cursor.number: String?
    get() =
      this.getString(this.getColumnIndex(Phone.NUMBER))

  private inline val Cursor.hasPhoneNumber: Boolean
    get() =
      this.getInt(this.getColumnIndex(Contacts.HAS_PHONE_NUMBER)) != 0


  val contacts: List<Contact>
    get() {
      val cursor = loadContacts()

      val list = mutableListOf<Contact>()
      for (pos: Int in 0 until cursor.count) {
        cursor.moveToPosition(pos)

        val name = cursor.displayName
        var phoneNumber: String? = null
        if (cursor.hasPhoneNumber) {
          var phonesCursor: Cursor? = null
          try {
            phonesCursor = getPhoneNumbers(cursor)

            if (phonesCursor != null && phonesCursor.moveToFirst()) {
              phoneNumber = phonesCursor.number
            }
          } finally {
            phonesCursor?.close()
          }
        }

        list += Contact(name, phoneNumber)
      }
      return list
    }

  private fun getPhoneNumbers(cursor: Cursor): Cursor? {
    return context.contentResolver.query(
        Phone.CONTENT_URI,
        PROJECTION_PHONE,
        Phone.LOOKUP_KEY + " = ? AND " + Phone.NUMBER + " != \"\"",
        arrayOf(cursor.lookupKey),
        Phone.IS_PRIMARY + " DESC")
  }
}