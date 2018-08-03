package com.adorablecontacts.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

  protected final LayoutInflater layoutInflater;
  private final List<T> items = new ArrayList<>();

  public BaseRecyclerAdapter(final LayoutInflater layoutInflater) {
    this.layoutInflater = layoutInflater;
  }

  public List<T> getItems() {
    return items;
  }

  public void setItems(final List<T> items) {
    this.items.clear();
    this.items.addAll(items);
    notifyDataSetChanged();
  }

  @Override
  public int getItemCount() {
    return getItems().size();
  }
}
