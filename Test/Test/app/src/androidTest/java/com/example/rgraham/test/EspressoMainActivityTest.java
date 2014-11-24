package com.example.rgraham.test;

import android.test.ActivityInstrumentationTestCase2;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;


import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by rgraham on 11/17/14.
 */
public class EspressoMainActivityTest extends ActivityInstrumentationTestCase2 {

    public EspressoMainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();

    }

    public void testTextViewHint(){

        onView(allOf(withId(R.string.hint_text), isDisplayed()));
    }

    public void testVerifyButtonLabel(){

        onView(allOf(withId(R.id.main_button),isDisplayed())).check(matches(withText(R.string.button)));
    }

    public void testErrorText(){
       onView(withId(R.id.main_button)).perform(click());
       onView(allOf(withId(R.id.main_textView), isDisplayed())).check(matches(withText("Unable to retrieve web page. URL may be invalid.")));
    }

    /* Tests that have yet to be written and / or Features to be implemented

    public void testErrorToast(){}
    public void testFeedLoadSuccessToast(){}
    public void testNetworkErrorToast(){}

    */

}

