/*
 * Copyright © 2018, DB Corp,
 *
 */

package com.android.videogallery.adapters;

import android.os.Bundle;

/**
 * Contains methods to notify the adapter.
 */
public interface RecyclerAdapterNotifier {
    void sendEvent(RecyclerAdapterViewHolder holder, Bundle data);
}
