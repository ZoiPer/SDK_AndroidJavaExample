package com.zoiper.zdk.android.demo.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.TextView;

import com.zoiper.zdk.android.demo.R;

/**
 * TextViewSelectionUtils
 *
 * @since 4.2.2019 г.
 */
public class TextViewSelectionUtils {

    /**
     * Selects textview by changing it icon and text colors to accent/white.
     *
     * @param view
     *         The selected textview
     * @param selected
     *         True if selected, false otherwise.
     */
    public static void setTextViewSelected(TextView view, boolean selected) {
        view.setSelected(selected);
        int color;
        if (selected) {
            color = getThemeAccentColor(view.getContext());
        } else {
            color = Color.WHITE;
        }
        Drawable[] drawables = view.getCompoundDrawables();
        if (drawables[1] != null) {  // left drawable
            drawables[1].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
        view.setTextColor(color);
    }

    /**
     * Returns the current theme Accent color.
     * @return The current Accent color.
     */
    private static int getThemeAccentColor(final Context context) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

}
