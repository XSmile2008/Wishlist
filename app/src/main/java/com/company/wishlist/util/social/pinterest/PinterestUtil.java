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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

public class PinterestUtil {

    private static final String HOST = "https://ru.pinterest.com";
    private static final String SEARCH_QUERY_LINK = HOST + "/search/pins/?q=%s";

    public interface PinterestOnLoadEvent {
        void onSuccess(List<String> urls);
    }

    public static List<String> getImagesAsLinks(String... query) {

        class ImageTask extends AsyncTask<String, Void, List<String>> {

            private List<String> urls = new CopyOnWriteArrayList<>();

            @Override
            protected List<String> doInBackground(String... params) {
                try {
                    for (String query : params) {
                        URL url = new URL(String.format(SEARCH_QUERY_LINK, URLEncoder.encode(query, "UTF-8")));
                        Document doc = Jsoup.parse(new Page(url).getContent());

                        List<Element> elements = doc.getElementsByClass("pinImg").not(".fade");

                        for (Element element : elements) {
                            urls.add(element.attr("src"));
                        }

                    }
                } catch (IOException ignored) {
                } finally {
                    return urls;
                }
            }


        }

        try {

            List<String> urls = new ImageTask().execute(query).get();
            Collections.shuffle(urls);

            return urls;
        } catch (InterruptedException | ExecutionException e) {
            Log.v("PinterestUtil", e.getMessage());
        }
        return null;
    }

    public static void getImagesAsLinks(PinterestOnLoadEvent event, String... queries) {
        if (null != event) {
            event.onSuccess(getImagesAsLinks(queries));
        } else {
            throw new IllegalArgumentException("PinterestOnLoadEvent should be not null!");
        }
    }


    private static List<String> getFilterData(List<String> urls) {
        List<String> result = new ArrayList<>();
        for (String url : urls) {
            if (url.startsWith("https") && url.contains("236x")) {
                result.add(url);
            }
        }
        return result;
    }

}
