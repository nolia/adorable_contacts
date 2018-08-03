package com.adorablecontacts.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adorablecontacts.R;
import com.adorablecontacts.data.Contact;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

@SuppressLint("Registered")
@EActivity(R.layout.activity_list)
public class ListActivity extends BaseActivity {

  private static final int REQUEST_READ_CONTACTS = 100;

  @ViewById
  RecyclerView recyclerView;
  @ViewById
  TextView infoTextView;
  @ViewById
  ProgressBar progressBar;


  @NonConfigurationInstance
  List<Contact> contactList;

  @InstanceState
  int savedScrollPosition;

  private ContactAdapter contactAdapter;
  private LinearLayoutManager layoutManager;

  @AfterViews
  void afterViews() {
    contactAdapter = new ContactAdapter(LayoutInflater.from(this));
    layoutManager = new LinearLayoutManager(this);

    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(contactAdapter);

    if (checkContactPermission()) {
      loadData();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    savedScrollPosition = layoutManager.findFirstVisibleItemPosition();
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
      loadData();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private void loadData() {
    disposeOnDestroy.add(
        getContacts()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(s -> showProgress(true))
            .doOnNext(items -> showProgress(false))
            .subscribe(
                list -> {
                  if (list.isEmpty()) {
                    showInfoMessage(getString(R.string.empty_list));
                    return;
                  }
                  contactAdapter.setItems(list);
                  layoutManager.scrollToPosition(savedScrollPosition);
                },

                throwable -> showInfoMessage(String.format("Error:\n%s", throwable.getMessage()))
            )
    );
  }

  private Observable<List<Contact>> getContacts() {
    return contactList != null
        ? Observable.just(contactList)
        : contactProvider.getContacts()
        .doOnNext(items -> contactList = items);
  }

  private void showInfoMessage(final String message) {
    infoTextView.setVisibility(View.VISIBLE);
    recyclerView.setVisibility(View.INVISIBLE);
    progressBar.setVisibility(View.INVISIBLE);
    infoTextView.setText(message);
  }

  private void showProgress(final boolean show) {
    progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    recyclerView.setVisibility(!show ? View.VISIBLE : View.INVISIBLE);

    infoTextView.setVisibility(View.INVISIBLE);
  }

  private void onOpenContact(final Contact contact) {
    DetailsActivity_.intent(this).contact(contact).start();
  }

  private class ContactAdapter extends BaseRecyclerAdapter<Contact, ContactViewHolder> {

    ContactAdapter(final LayoutInflater layoutInflater) {
      super(layoutInflater);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
      return new ContactViewHolder(layoutInflater.inflate(R.layout.item_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactViewHolder holder, final int position) {
      final Contact contact = getItems().get(position);

      holder.nameTextView.setText(contact.getName());
      holder.phoneTextView.setText(contact.getPhoneNumber());

      avatarProvider.loadAvatar(contact, holder.avatarImageView);

      holder.itemView.setOnClickListener(v -> onOpenContact(contact));

    }
  }

  private static class ContactViewHolder extends RecyclerView.ViewHolder {

    private final TextView nameTextView;
    private final TextView phoneTextView;
    private final ImageView avatarImageView;

    ContactViewHolder(final View itemView) {
      super(itemView);
      nameTextView = itemView.findViewById(R.id.name);
      phoneTextView = itemView.findViewById(R.id.phone);
      avatarImageView = itemView.findViewById(R.id.avatar);
    }
  }

}
