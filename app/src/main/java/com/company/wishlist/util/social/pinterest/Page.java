package com.company.wishlist.util.social.pinterest;

import java.io.IOException;
import java.net.URL;


public class Page {

    private String content;
    private URL url;

    public Page(URL url) throws IOException {
        this.url = url;
        this.content = new String(ConnectionUtils.getData(url));
    }

    public String getContent() {
        return content;
    }
}
