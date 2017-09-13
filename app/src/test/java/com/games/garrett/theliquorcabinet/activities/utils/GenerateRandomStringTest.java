package com.games.garrett.theliquorcabinet.activities.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Garrett on 8/23/2017.
 */

public class GenerateRandomStringTest {

    String randomString;
    String randomString2;

    @Before
    public void createRandomString(){
    }

    @Test
    public void correct_length() throws Exception {
        randomString = GenerateRandomString.randomString(10);
        assertEquals(randomString.length(), 10);
    }

    @Test
    public void handle_zero_length() throws Exception {
        randomString = GenerateRandomString.randomString(0);
        assertEquals(randomString.length(), 0);
    }


    @Test(expected = IllegalArgumentException.class)
    public void handle_negative_length() throws Exception {
        randomString = GenerateRandomString.randomString(-1);
    }

    /* Statistically, these two values should not be equal
        although, technically possible.
     */
    @Test
    public void uniqueness_between_two_consecutive_calls() throws Exception {
        randomString = GenerateRandomString.randomString(30);
        randomString2 = GenerateRandomString.randomString(30);
        assertNotEquals(randomString, randomString2);
    }
}
