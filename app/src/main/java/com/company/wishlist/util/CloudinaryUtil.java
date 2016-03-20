package com.company.wishlist.util;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vladstarikov on 06.03.16.
 */
public class CloudinaryUtil {

    private static final String CLOUD_NAME = "divbmhmwt";
    private static final String API_KEY = "225659278967932";
    private static final String API_SECRET = "_wgckYIXYW8ly46LijKF47UqoUI";

    public static Cloudinary getInstance() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", CLOUD_NAME);
        config.put("api_key", API_KEY);
        config.put("api_secret", API_SECRET);
        return new Cloudinary(config);
    }

    public static String getPublicId(String ulr) {
        return ulr.substring(ulr.lastIndexOf('/') + 1, ulr.lastIndexOf('.'));
    }

    public static void upload(String url) {
        new AsyncUploader(null).execute(url);
    }

    public static void upload(InputStream inputStream) {
        new AsyncUploader(null).execute(inputStream);
    }

    public static void upload(String url, IOnDoneListener listener) {
        new AsyncUploader(listener).execute(url);
    }

    public static void upload(InputStream inputStream, IOnDoneListener listener) {
        new AsyncUploader(listener).execute(inputStream);
    }

    public static void destroy(String publicId) {
        destroy(publicId, null);
    }

    public static void destroy(final String publicId, final IOnDoneListener listener) {
        new AsyncTask<Void, Void, Void>() {//TODO: replace async task for anything else
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Map map = getInstance().uploader().destroy(publicId, ObjectUtils.emptyMap());
                    if (listener != null) listener.onDone(map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public static String getThumbURl(String publicId, int wight, int height) {
        Transformation transformation = new Transformation().width(wight).height(height).crop("lfill");
        return getInstance().url().transformation(transformation).format("jpg").generate(publicId);
    }

    public static void loadThumb(final Context context, final ImageView imageView, final String publicId, @Nullable @DrawableRes final Integer placeholder, final boolean circle) {
        ViewTreeObserver observer = imageView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                DrawableRequestBuilder builder = Glide.with(context)
                        .load(CloudinaryUtil.getThumbURl(publicId, imageView.getWidth(), imageView.getHeight()));
                if (circle) builder.bitmapTransform(new CropCircleTransformation(Glide.get(context).getBitmapPool()));
                if (placeholder != null) builder.placeholder(placeholder);
                builder.into(imageView);
                return true;
            }
        });
    }

    //TODO: try use loaders
    private static class AsyncUploader extends AsyncTask<Object, Void, Void> {

        IOnDoneListener listener;

        public AsyncUploader(@Nullable IOnDoneListener listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Object... params) {
            try {
                Map options = ObjectUtils.emptyMap();
                Map map = CloudinaryUtil.getInstance().uploader().upload(params[0], options);
                if (listener != null) listener.onDone(map);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public interface IOnDoneListener {
        void onDone(Map<String, Object> imgInfo);
    }

}
