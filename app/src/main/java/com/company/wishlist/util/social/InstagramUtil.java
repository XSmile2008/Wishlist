package com.company.wishlist.util.social;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.company.wishlist.model.Wish;

import org.jinstagram.Instagram;
import org.jinstagram.entity.common.Images;
import org.jinstagram.entity.tags.TagMediaFeed;
import org.jinstagram.entity.tags.TagSearchFeed;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by v.odahovskiy on 20.01.2016.
 */
public class InstagramUtil {

    private static InstagramUtil instance;
    private static final int CNT_PICS_FOR_TAG = 25;

    public static synchronized InstagramUtil getInstance() {
        if (instance == null) {
            instance = new InstagramUtil();
        }
        return instance;
    }

    public List<Images> getPicturesByTag(String... tags) {
        final List<Images> result = new ArrayList<>();
        final int countPicsPerTag = CNT_PICS_FOR_TAG / tags.length;

        class DownloadImageTask extends AsyncTask<String, Void, List<Images>> {
            List<Images> images = new ArrayList<>();

            protected List<Images> doInBackground(String... tags) {
                Instagram instagram = new Instagram("a3d07a3cc65445e08ebd6dc958fe6b05");

                for (final String tag : tags) {
                    TagMediaFeed tagMediaFeed = null;
                    try {
                        tagMediaFeed = instagram.getRecentMediaTags(tag, countPicsPerTag);
                        if (null != tagMediaFeed) {
                            for (MediaFeedData mfd : instagram.getRecentMediaTags(tag, countPicsPerTag).getData()) {
                                images.add(mfd.getImages());
                            }
                        }
                    } catch (InstagramException e) {
                        Log.d("instagram", e.getMessage());
                    }
                }
                return images;
            }
        }

        try {
            result.addAll(new DownloadImageTask().execute(tags).get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Log.d("bla", "Load " + result.size() + " pics");
        return result;
    }

}
