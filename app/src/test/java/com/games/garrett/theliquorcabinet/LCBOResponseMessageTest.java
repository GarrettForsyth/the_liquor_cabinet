package com.games.garrett.theliquorcabinet;

import android.content.Context;
import android.location.Criteria;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;;
import android.test.ActivityTestCase;


import com.games.garrett.theliquorcabinet.services.utils.ResponseMessage;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResult;
import com.games.garrett.theliquorcabinet.services.utils.ServiceResultHandler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Created by Garrett on 7/22/2017.
 */

public class LCBOResponseMessageTest {

    private final int TEST_REQUEST_CODE = 1;
    private ArrayList<String> TEST_DATA = new ArrayList<>(Arrays.asList("test1","test2"));
    
    private Uri mockUri;
    private  Messenger mockMessenger;

    @Before
    public void setup(){
        mockMessenger = mock(Messenger.class);
        mockUri = mock(Uri.class);
    }

    @Test
    public void should_send_one_message() throws Exception{
        //ResponseMessage.sendEntries(mockMessenger,TEST_DATA.get(0),  mockUri, TEST_REQUEST_CODE);
        //verify(mockMessenger, times(1)).send((Message) any());
    }

}
