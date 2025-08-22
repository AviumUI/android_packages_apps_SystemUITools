package android.view;

import android.graphics.Bitmap;

/**
 * This file must be an exact copy of the one in the framework.
 * {@hide}
 */
oneway interface IViewCaptureCallback {
    void onViewsCaptured(in List<Bitmap> bitmaps);
}