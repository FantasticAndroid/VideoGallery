/*
 * Copyright © 2018, DB Corp,
 *
 */

package com.android.videogallery.adapters;

/**
 * Listener invoked for every element that is going to be removed.
 */
public interface RemoveListener {
    boolean hasToBeRemoved(AdapterItem item);
}
