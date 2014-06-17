package com.github.pradeeppappu.apdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by pappu on 13/06/2014.
 */
public class Utils {
    /**
     * Used to get a bitmap from a given dataUrl.
     * @param dataUrl
     * @return
     */
    public static Bitmap getBitmap(String dataUrl, Context ctx, int quality) {
        String encodedData = dataUrl.substring(dataUrl.indexOf(",") + 1);
        byte[] decodedString = Base64.decode(encodedData, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        try {
            baos.flush();
            bitmap.recycle();
            byte[] imageData = baos.toByteArray();
            baos.close();
            BitmapDrawable bmd = new BitmapDrawable(ctx.getResources(), new ByteArrayInputStream(imageData));
            return bmd.getBitmap();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bitmap.", e);
        }
    }

    public static String readFileFromRawAssets(Context context, int rawFileId) {
        try {
            InputStream is = context.getResources().openRawResource(rawFileId);
            return convertStreamToString(is);
        } catch (IOException e) {
            return "";
        }
    }

    private static String convertStreamToString(InputStream is) throws IOException {
        Writer writer = new StringWriter();

        char[] buffer = new char[2048];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        String text = writer.toString();
        return text;
    }
}
