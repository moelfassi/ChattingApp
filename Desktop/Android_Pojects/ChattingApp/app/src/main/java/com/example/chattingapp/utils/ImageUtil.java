package com.example.chattingapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.example.chattingapp.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.FileNotFoundException;

/**
 * Created by elfassimounir on 3/29/16.
 */
public class ImageUtil {

    private static final DisplayImageOptions.Builder DEFAULT_DISPLAY_IMAGE_OPTIONS_BUIDLER = new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
            .displayer(new FadeInBitmapDisplayer(300, true, false, false))
            .showImageForEmptyUri(R.drawable.profile)
            .showImageOnLoading(R.drawable.profile)
            .showImageOnFail(R.drawable.profile).cacheOnDisk(true)
            .cacheInMemory(true).bitmapConfig(Bitmap.Config.ARGB_8888);

    private static final DisplayImageOptions DEFAULT_DISPLAY_IMAGE_OPTIONS = DEFAULT_DISPLAY_IMAGE_OPTIONS_BUIDLER.build();
    private static final DisplayImageOptions ROUND_DISPLAY_IMAGE_OPTIONS = DEFAULT_DISPLAY_IMAGE_OPTIONS_BUIDLER
            .displayer(new RoundedBitmapDisplayer(500)).build();

    public static void displayImage(ImageView view, String path, ImageLoadingListener listener) {
        ImageLoader loader = ImageLoader.getInstance();
        try {
            loader.displayImage(path, view, DEFAULT_DISPLAY_IMAGE_OPTIONS, listener);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            loader.clearMemoryCache();
        }
    }

    public static void displayRoundImage(ImageView view, String path, ImageLoadingListener listener) {
        ImageLoader loader = ImageLoader.getInstance();
        try {
            loader.displayImage(path, view, ROUND_DISPLAY_IMAGE_OPTIONS,
                    listener);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            loader.clearMemoryCache();
        }
    }

    public static void loadImage(String path, ImageLoadingListener listener) {
        ImageLoader loader = ImageLoader.getInstance();
        try {
            loader.loadImage(path, DEFAULT_DISPLAY_IMAGE_OPTIONS, listener);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth
                , height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }
}
