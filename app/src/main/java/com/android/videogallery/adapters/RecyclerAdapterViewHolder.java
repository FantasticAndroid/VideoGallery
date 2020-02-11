/*
 * Copyright © 2018, DB Corp,
 *
 */

package com.android.videogallery.adapters;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

/**
 * Base ViewHolder class to extend in subclasses.
 */
public abstract class RecyclerAdapterViewHolder extends RecyclerView.ViewHolder {

    private final WeakReference<RecyclerAdapterNotifier> adapter;

    /***
     *
     * @param view
     * @param adapter
     */
    public RecyclerAdapterViewHolder(View view, RecyclerAdapterNotifier adapter) {
        super(view);
        this.adapter = new WeakReference<>(adapter);
    }

    /**
     * Sends an event to the adapter.
     * @param data additional event data
     */
    public final void sendEvent(Bundle data) {
        this.adapter.get().sendEvent(this, data);
    }

    protected final View findViewById(int id) {
        return itemView.findViewById(id);
    }
}
