package com.company.wishlist.util.social.pinterest;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

public class PinterestUtil {

    private static final String HOST = "https://ru.pinterest.com";
    private static final String SEARCH_QUERY_LINK = HOST + "/search/pins/?q=%s";

    public static void getImagesAsLinks(IOnDoneListener listener, String... query) {
        new ImageTask(listener).execute(query);
    }

    public static String getOriginal(String url) {
        return url.replace("236x", "originals");
    }

    private static class ImageTask extends AsyncTask<String, Void, Void> {

        private IOnDoneListener listener;

        public ImageTask(IOnDoneListener listener) {
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(String... params) {
            List<String> urls = new ArrayList<>();
            try {
                for (String query : params) {
                    URL url = new URL(String.format(SEARCH_QUERY_LINK, URLEncoder.encode(query, "UTF-8")));
                    Document doc = Jsoup.parse(new Page(url).getContent());
                    for (Element element : doc.getElementsByClass("pinImg").not(".fade")) {
                        String imgUrl = element.attr("src");
                        urls.add(imgUrl);
                    }
                }
            } catch (IOException ignored) {
            } finally {
                if (listener != null) listener.onDone(urls);
            }
            return null;
        }

    }

    public interface IOnDoneListener {
        void onDone(List<String> urls);
    }

}
