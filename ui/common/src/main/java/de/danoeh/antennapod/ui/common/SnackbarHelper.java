package de.danoeh.antennapod.ui.common;

import android.view.View;
import androidx.annotation.StringRes;
import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.snackbar.Snackbar;

public final class SnackbarHelper {

    public static Snackbar make(View view, CharSequence text, int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        snackbar.getBehavior().setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY);
        return snackbar;
    }

    public static Snackbar make(View view, @StringRes int resId, int duration) {
        Snackbar snackbar = Snackbar.make(view, resId, duration);
        snackbar.getBehavior().setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY);
        return snackbar;
    }

    private SnackbarHelper() {
    }
}
