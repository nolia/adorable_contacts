package com.adorablecontacts.ui;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adorablecontacts.R;
import com.adorablecontacts.data.Contact;
import com.adorablecontacts.data.ContactDetailsItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

@SuppressLint("Registered")
@EActivity(R.layout.activity_details)
public class DetailsActivity extends BaseActivity {

  @ViewById
  RecyclerView recyclerView;

  @ViewById
  ProgressBar progressBar;

  @ViewById(R.id.avatar)
  ImageView avatarImageView;

  @ViewById(R.id.name)
  TextView nameTextView;

  @Extra
  Contact contact;

  @NonConfigurationInstance
  List<ContactDetailsItem> detailsItemList;

  @InstanceState
  int savedScrollPosition;

  private DetailsAdapter detailsAdapter;
  private LinearLayoutManager layoutManager;

  @AfterViews
  void afterViews() {
    final ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.setDisplayHomeAsUpEnabled(true);
    }

    detailsAdapter = new DetailsAdapter(LayoutInflater.from(this));
    layoutManager = new LinearLayoutManager(this);

    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(detailsAdapter);

    // Bind contact data.
    avatarProvider.loadAvatar(contact, avatarImageView);
    nameTextView.setText(contact.getName());

    loadData();
  }

  @Override
  protected void onPause() {
    super.onPause();
    savedScrollPosition = layoutManager.findFirstVisibleItemPosition();
  }

  private void loadData() {
    disposeOnDestroy.add(
        getContactDetails()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(s -> showProgress(true))
            .doFinally(() -> showProgress(false))
            .subscribe(
                items -> {
                  detailsAdapter.setItems(items);
                  layoutManager.scrollToPosition(savedScrollPosition);
                },
                throwable -> Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show()
            )
    );
  }

  private Observable<List<ContactDetailsItem>> getContactDetails() {
    return detailsItemList != null
        ? Observable.just(detailsItemList)
        : contactProvider.getContactDetails(contact)
        .doOnNext(items -> detailsItemList = items);
  }

  private void showProgress(final boolean show) {
    progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    recyclerView.setVisibility(!show ? View.VISIBLE : View.INVISIBLE);
  }

  private class DetailsAdapter extends BaseRecyclerAdapter<ContactDetailsItem, DetailsViewHolder> {

    DetailsAdapter(final LayoutInflater layoutInflater) {
      super(layoutInflater);
    }

    @NonNull
    @Override
    public DetailsViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
      return new DetailsViewHolder(layoutInflater.inflate(R.layout.item_details_data, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final DetailsViewHolder holder, final int position) {
      final ContactDetailsItem detailsItem = getItems().get(position);

      holder.dataTextView.setText(detailsItem.getData());
      holder.infoTextView.setText(detailsItem.getInfo());
    }
  }

  private static class DetailsViewHolder extends RecyclerView.ViewHolder {

    private TextView dataTextView;
    private TextView infoTextView;

    DetailsViewHolder(final View itemView) {
      super(itemView);
      dataTextView = itemView.findViewById(R.id.data);
      infoTextView = itemView.findViewById(R.id.info);
    }
  }

}
