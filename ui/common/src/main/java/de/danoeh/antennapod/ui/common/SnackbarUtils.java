package de.danoeh.antennapod.ui.common;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.annotation.StringRes;

import com.google.android.material.snackbar.Snackbar;

/**
 * Utilities for creating snackbars with consistent app-wide behavior.
 */
public final class SnackbarUtils {

    public static Snackbar make(View view, @StringRes int resId, int duration) {
        return enableSwipeToDismiss(Snackbar.make(view, resId, duration));
    }

    public static Snackbar make(View view, CharSequence text, int duration) {
        return enableSwipeToDismiss(Snackbar.make(view, text, duration));
    }

    @SuppressLint("ClickableViewAccessibility")
    private static Snackbar enableSwipeToDismiss(Snackbar snackbar) {
        snackbar.getView().setOnTouchListener(new SwipeToDismissTouchListener(snackbar));
        return snackbar;
    }

    private static final class SwipeToDismissTouchListener implements View.OnTouchListener {
        private final Snackbar snackbar;
        private final int touchSlop;
        private float downRawX;
        private int viewWidth = 1;
        private boolean swiping;

        SwipeToDismissTouchListener(Snackbar snackbar) {
            this.snackbar = snackbar;
            this.touchSlop = ViewConfiguration.get(snackbar.getView().getContext()).getScaledTouchSlop();
        }

        @Override
        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downRawX = event.getRawX();
                    viewWidth = Math.max(1, view.getWidth());
                    return false;
                case MotionEvent.ACTION_MOVE: {
                    float deltaX = event.getRawX() - downRawX;
                    if (!swiping && Math.abs(deltaX) > touchSlop) {
                        swiping = true;
                        ViewParent parent = view.getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    if (swiping) {
                        view.setTranslationX(deltaX);
                        view.setAlpha(Math.max(0f, 1f - Math.abs(deltaX) / viewWidth));
                        return true;
                    }
                    return false;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    if (!swiping) {
                        return false;
                    }
                    swiping = false;
                    float deltaX = event.getRawX() - downRawX;
                    if (Math.abs(deltaX) > viewWidth / 3f) {
                        view.animate()
                                .translationX(deltaX > 0 ? viewWidth : -viewWidth)
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction(snackbar::dismiss);
                    } else {
                        view.animate().translationX(0f).alpha(1f).setDuration(200);
                    }
                    return true;
                }
                default:
                    return false;
            }
        }
    }

    private SnackbarUtils() {
        /* Utility classes should not be instantiated */
    }
}
