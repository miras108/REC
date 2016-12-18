package com.web.crowler;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * Created by miras108 on 2016-12-17.
 */
public class CrawlerTest {

    @Test
    public void testStreetResolver() throws UnsupportedEncodingException {
        String text = "AACZ AS - AS";
        String text2 = text.replaceAll("[^a-zA-Z]", "");
        System.out.println(text2);
    }


}