package com.github.pradeeppappu.apdf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

/**
 * Created by pappu on 13/06/2014.
 */
public class Utils {
    /**
     * Used to get a bitmap from a given dataUrl.
     * @param dataUrl
     * @return
     */
    public static Bitmap getBitmap(String dataUrl) {
        String encodedData = dataUrl.substring(dataUrl.indexOf(",") + 1);
        byte[] decodedString = Base64.decode(encodedData, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
