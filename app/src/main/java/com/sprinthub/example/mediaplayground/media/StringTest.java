package com.sprinthub.example.mediaplayground.media;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class StringTest {

    public static void main(String[] args) {
        System.out.println("----- Encoding .... I am Jon ------");
        System.out.println(urlEncode("I am Jon"));

        String mediaId = "1234_yweyhh12_36y_3ss";
        System.out.println(mediaId.substring(0, mediaId.indexOf("_")));
    }

    private static String urlEncode(String string) {
        if (Charset.isSupported("UTF-8")) {
            try {
                return URLEncoder.encode(string, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return string;
            }
        } else return URLEncoder.encode(string);
    }
}
