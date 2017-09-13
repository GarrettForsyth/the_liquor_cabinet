package com.games.garrett.theliquorcabinet.activities.utils;

import java.util.Random;

/**
 * Class used to generate a random alpha-numeric string.
 * Used to create unique user ids.
 * Created by Garrett on 8/15/2017.
 */

public class GenerateRandomString {

    /* Define the set of characters to rnadomly pick from */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String CHARACTER_SET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /* Create the random generator */
    private static Random RANDOM = new Random();

    /**
     * Creates a random string from a set of characters defined in the character set.
     * @param stringLength   the length of the string.
     * @return      a random sting of length l
     */
    public static String randomString(int stringLength) {
        if (stringLength < 0) throw new IllegalArgumentException();
        StringBuilder sb = new StringBuilder(stringLength);

        for (int i = 0; i < stringLength; i++) {
            sb.append(CHARACTER_SET.charAt(RANDOM.nextInt(CHARACTER_SET.length())));
        }

        return sb.toString();
    }

}