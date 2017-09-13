package com.games.garrett.theliquorcabinet;


import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.games.garrett.theliquorcabinet.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Checks buttons exist with proper labels
 * Created by Garrett on 7/20/2017.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {

    @Rule
    public IntentsTestRule<MainActivity> mIntentsRule =
            new IntentsTestRule<>(MainActivity.class);

    @Test
    public void should_contain_browse_button(){
        onView(withId(R.id.browse_button)).check(matches(withText("BROWSE")));
    }

    @Test
    public void should_contain_recommend_button(){
        onView(withId(R.id.recommend_button)).check(matches(withText("RECOMMEND")));
    }

    @Test
    public void should_contain_liquor_logs_button(){
        onView(withId(R.id.liquor_logs_button)).check(matches(withText("LIQUOR LOGS")));
    }

    @Test
    public void should_contain_settings_button(){
        onView(withId(R.id.settings_button)).check(matches(withText("SETTINGS")));
    }

    @Test
    public void browse_button_should_launch_browse_activity(){
        //onView(withId(R.id.browse_button)).perform(click());
       // intended(hasComponent(BrowseActivity.class.getName()));
    }
}

