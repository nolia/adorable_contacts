package com.adorablecontacts.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.adorablecontacts.R;

public class MainActivity extends AppCompatActivity {

  private static final int REQUEST_READ_CONTACTS = 100;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);

    if (checkContactPermission()) {
      showContacts();
    }
  }

  private boolean checkContactPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          new String[]{Manifest.permission.READ_CONTACTS},
          REQUEST_READ_CONTACTS);
      return false;
    }
    return true;
  }

  @Override
  public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
    if (requestCode == REQUEST_READ_CONTACTS) {
      showContacts();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private void showContacts() {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragmentContainer, new ContactListFragment())
        .commit();
  }
}
