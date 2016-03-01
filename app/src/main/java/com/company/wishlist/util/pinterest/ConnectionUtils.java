package com.company.wishlist.util.pinterest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;


public class ConnectionUtils {
    private static final int BUFFER_SIZE_IN_KB = 2 * 1024;

    public static byte[] getData(URL url) throws IOException {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE_IN_KB];

        URLConnection connection = url.openConnection();
        BufferedInputStream reader = null;
        int len;
        try {
            reader = new BufferedInputStream(connection.getInputStream());
            while ((len = reader.read(buffer)) > 0) {
                bytes.write(buffer, 0, len);
            }
        } finally {
            if (null != reader) {
                reader.close();
            }
        }

        return bytes.toByteArray();
    }
}