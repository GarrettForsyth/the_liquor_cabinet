package com.games.garrett.theliquorcabinet;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.games.garrett.theliquorcabinet.activities.BrowseActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;

/**
 * UI test for browse activity.
 * Created by Garrett on 7/25/2017.
 */
@RunWith(AndroidJUnit4.class)
public class BrowseActivityUITest {

    @Rule
    public ActivityTestRule<BrowseActivity> activityActivityTestRule =
            new ActivityTestRule<>(BrowseActivity.class);

    @Test
    public void should_contain_search_query_edit_text(){
        onView(withId(R.id.query_edit_text))
                .perform(typeText("Test Query"), closeSoftKeyboard());
        onView(withId(R.id.query_edit_text)).check(matches(withText("Test Query")));
    }

    @Test
    public void should_contain_sort_by_spinner(){

        String[] apiOrder = {  "Price",
                                "Sale End Date",
                                "Package Volume",
                                "Package Units",
                                "Volume",
                                "Alcohol Content",
                                "Price per Liter",
                                "Price per Alcohol Liter",
                                "Release Date" };

        for(String selectionText : apiOrder){
            onView(withId(R.id.sort_by_spinner)).perform(click());
            onData(allOf(is(instanceOf(String.class)), is(selectionText))).perform(click());
            onView(withId(R.id.sort_by_spinner)).check(matches(withSpinnerText(containsString(selectionText))));
        }

    }

    @Test
    public void should_have_search_button(){
        onView(withId(R.id.search_button))
                .check(matches(withText("Search")));
    }



}
