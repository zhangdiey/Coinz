package uk.ac.ed.inf.coinz

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
* This test is to test MainActivity UI
* */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun preprocess(){
        resetDB()
    }
    @Test
    fun signIn() {
        onView(withId(R.id.btnSignIn))         // withId(R.id.my_view) is a ViewMatcher
                .perform(click())                // click() is a ViewAction
                .check(matches(isDisplayed())) // matches(isDisplayed()) is a ViewAssertion
    }

    @Test
    fun signUp() {
        onView(withId(R.id.btnSignUp))         // withId(R.id.my_view) is a ViewMatcher
                .perform(click())                // click() is a ViewAction
                .check(matches(isDisplayed())) // matches(isDisplayed()) is a ViewAssertion
    }
}