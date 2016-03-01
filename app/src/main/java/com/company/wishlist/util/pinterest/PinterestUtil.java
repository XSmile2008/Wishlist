package com.company.wishlist.util.pinterest;


import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class PinterestUtil {

    private static final String HOST = "https://ru.pinterest.com";
    private static final String SEARCH_QUERY_LINK = HOST + "/search/pins/?q=%s";


    public static Set<String> getImagesAsLinks(String query) {

        class ImageTask extends AsyncTask<String, Void, Set<String>> {

            @Override
            protected Set<String> doInBackground(String... params) {
                Set<String> urls = new HashSet<>();

                try {
                    URL url = new URL(String.format(SEARCH_QUERY_LINK, params));
                    Document doc = Jsoup.parse(new Page(url).getContent());
                    Elements images = doc.select("img");
                    Iterator<org.jsoup.nodes.Element> iterator = images.iterator();
                    while (iterator.hasNext()) {
                        urls.add(iterator.next().attr("src"));
                    }
                    return urls;
                } catch (MalformedURLException e) {
                    Log.v("PinterestUtil", e.getMessage());
                    return urls;
                } catch (IOException e) {
                    return urls;
                }
            }
        }

        try {
            Set<String> urls = new ImageTask().execute(query).get();
            //filter(urls);
            System.out.println(urls.size());
        } catch (InterruptedException | ExecutionException e) {
            Log.v("PinterestUtil", e.getMessage());
        }
        return null;
    }

    private static Set<String> filter(Set<String> urls) {
        for (String url : urls) {
            if (!url.startsWith("https")) {
                urls.remove(url);
            }
        }
        return urls;
    }

}
