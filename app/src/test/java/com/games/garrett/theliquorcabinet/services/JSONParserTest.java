package com.games.garrett.theliquorcabinet.services;

import com.games.garrett.theliquorcabinet.services.utils.JSONParser;

import org.junit.Before;
import org.junit.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Test the makeHttpRequest method in the JSONParser and implicitly the
 * private methods helping it.
 *
 * The method should return a JSONOBject that is the response of the
 * server to the request.
 *
 * Created by Garrett on 8/25/2017.
 */

public class JSONParserTest {

    JSONParser jsonParser;
    private final String TEST_SERVER_RESPONSE = "{success: 1, message: Test Successful!'}";
    private MockWebServer server;

    /* Create a server with a mock response and a new JSONParser Object */
    @Before
    public void setup() throws  Exception{
        jsonParser = new JSONParser();
        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(TEST_SERVER_RESPONSE);

        server = new MockWebServer();
        server.enqueue(response);
        server.start();
    }


}
