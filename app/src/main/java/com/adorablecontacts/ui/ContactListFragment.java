package com.adorablecontacts.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adorablecontacts.App;
import com.adorablecontacts.R;
import com.adorablecontacts.data.AvatarProvider;
import com.adorablecontacts.data.Contact;
import com.adorablecontacts.data.ContactProvider;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class ContactListFragment extends Fragment {

  private RecyclerView recyclerView;
  private TextView infoTextView;
  private ProgressBar progressBar;

  private AvatarProvider avatarProvider;
  private ContactProvider contactProvider;

  private CompositeDisposable disposeOnDestroy = new CompositeDisposable();
  private ContactAdapter contactAdapter;

  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final App application = (App) getActivity().getApplication();
    if (application == null) {
      throw new IllegalStateException("application == null");
    }
    avatarProvider = application.getAvatarProvider();
    contactProvider = application.getContactProvider();

  }

  @Nullable
  @Override
  public View onCreateView(@NonNull final LayoutInflater inflater,
                           @Nullable final ViewGroup container,
                           @Nullable final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fagment_list, container, false);
  }

  @Override
  public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
    infoTextView = view.findViewById(R.id.infoTextView);
    progressBar = view.findViewById(R.id.progressBar);

    recyclerView = view.findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    contactAdapter = new ContactAdapter(LayoutInflater.from(getContext()), avatarProvider);
    recyclerView.setAdapter(contactAdapter);

    loadData();
  }

  @Override
  public void onDestroy() {
    disposeOnDestroy.dispose();
    super.onDestroy();
  }

  private void loadData() {
    disposeOnDestroy.add(
        contactProvider
            .getContacts()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(s -> showProgress(true))
            .doFinally(() -> showProgress(false))
            .subscribe(
                list -> {
                  if (list.isEmpty()) {
                    infoTextView.setText(R.string.empty_list);
                    return;
                  }
                  contactAdapter.setContactList(list);
                },

                throwable -> infoTextView.setText(String.format("Error:\n%s", throwable.getMessage()))
            )
    );
  }

  private void showProgress(final boolean show) {
    progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    recyclerView.setVisibility(!show ? View.VISIBLE : View.INVISIBLE);
  }

  private static class ContactAdapter extends RecyclerView.Adapter<ContactViewHolder> {

    private final LayoutInflater inflater;
    private final AvatarProvider avatarProvider;

    private List<Contact> contactList = new ArrayList<>();

    public ContactAdapter(final LayoutInflater inflater, final AvatarProvider avatarProvider) {
      this.inflater = inflater;
      this.avatarProvider = avatarProvider;
    }

    public void setContactList(final List<Contact> contactList) {
      this.contactList = contactList;
      notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
      return new ContactViewHolder(inflater.inflate(R.layout.item_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactViewHolder holder, final int position) {
      final Contact contact = contactList.get(position);

      holder.nameTextView.setText(contact.getName());
      holder.phoneTextView.setText(contact.getPhoneNumber());

      avatarProvider.loadAvatar(contact, holder.avatarImageView);
    }

    @Override
    public int getItemCount() {
      return contactList.size();
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
