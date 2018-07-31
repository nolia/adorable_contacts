package com.adorablecontacts.data;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class AvatarProvider {

  private static final String URI_FORMAT = "https://api.adorable.io/avatars/285/%s.png";

  private final Context context;


  public AvatarProvider(final Context context) {
    this.context = context;
  }


  public void loadAvatar(final Contact contact, final ImageView target) {
    Glide.with(context)
        .load(buildUri(contact))
        .into(target);
  }

  private String buildUri(final Contact contact) {
    // Create a hash string id for each contact, making requests for
    // avatars anonymous.
    String hash = Integer.toString(contact.getName().hashCode(), 16);
    if (contact.getPhoneNumber() != null) {
      hash += Integer.toString(contact.getPhoneNumber().hashCode(), 16);
    }

    return String.format(URI_FORMAT, hash);
  }
}
