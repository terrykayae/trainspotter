package uk.co.tezk.trainspotter;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by tezk on 23/05/17.
 */

public class InitalViewTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    @Test
    public void mainActivityIsDisplayed() {
        Espresso.onView(withId(R.id.mainContainer)).check(matches(isDisplayed()));
    }

    @Test
    public void recyclerViewLoadsCardviews() throws InterruptedException {
        Espresso.onView(withId(R.id.classListRecyclerview)).check(matches(isDisplayed()));
    }

    @Test
    public void testClickOnItem() throws InterruptedException {
        Espresso.onView(withId(R.id.classListRecyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        Thread.sleep(2000);
        Espresso.onView(withId(R.id.trainListRecyclerView)).check(matches(isDisplayed()));
    }
}

