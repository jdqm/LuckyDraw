package com.jdqm.luckydraw;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by Jdqm on 2018-1-24.
 */

public class DisplayUtil {
    public static int dpToPixel(float dpValue) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (dpValue * metrics.density);
    }

    public static int spToPixel(float spValue) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) (spValue * metrics.scaledDensity);
    }
}
