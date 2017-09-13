package com.games.garrett.theliquorcabinet;

import android.net.Uri;

import com.games.garrett.theliquorcabinet.services.utils.LCBODownload;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertEquals;
import static org.bouncycastle.crypto.tls.ConnectionEnd.client;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created by Garrett on 7/24/2017.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({Uri.class})
public class LCBODownloadTest {

    private final String TEST_RESPONSE = "{\"pager\":{\"is_final_page\":true},\"result\":[{\"testNumb\":42}]}";
    private MockWebServer server;

    @Before
    public void setup() throws  Exception{
        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(TEST_RESPONSE);

        server = new MockWebServer();
        server.enqueue(response);
        server.start();

        PowerMockito.mockStatic(Uri.class);
        Uri uri = mock(Uri.class);

        PowerMockito.when(Uri.class, "parse", anyString()).thenReturn(uri);
        // set toString() to return the root url
        // this is needed when makeUrl() calls url.toString()
        PowerMockito.when(uri.toString()).thenReturn(server.url("").toString());

    }

    @After
    public void teardown() throws Exception{
        server.shutdown();
    }

    @Test
    public void return_first_page() throws Exception{

        HttpUrl baseUrl = server.url("");
        Uri url = Uri.parse(baseUrl.toString());

       // assertEquals(TEST_RESPONSE,LCBODownload.downloadLCBOItems(url).get(0));

    }

}
