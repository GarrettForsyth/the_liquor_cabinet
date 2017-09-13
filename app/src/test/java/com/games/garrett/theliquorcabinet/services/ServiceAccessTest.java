package com.games.garrett.theliquorcabinet.services;

import android.support.v4.util.Pair;

import com.games.garrett.theliquorcabinet.services.utils.ServiceAccess;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Garrett on 8/25/2017.
 */

public class ServiceAccessTest {

    private final String TEST_URL_STRING = "http://www.testLL";
    private List<Pair<String,String>> testParams;

    @Before
    public void setup(){
        setupTestParams();
    }

    private void setupTestParams(){
        Pair<String,String> param1 = new Pair<>("userID", "001");
        Pair<String,String> param2 = new Pair<>("productID", "AAA");
        testParams = new ArrayList<>();
        testParams.add(param1);
        testParams.add(param2);
    }

    @Test
    public void test_createUrlStringFromParams() throws UnsupportedEncodingException{
        String expectedUrl = "http://www.testLL?userID=001&productID=AAA";
        String url = ServiceAccess.createUrlStringFromParams(TEST_URL_STRING, testParams);

        assertEquals(expectedUrl, url);

    }
}
