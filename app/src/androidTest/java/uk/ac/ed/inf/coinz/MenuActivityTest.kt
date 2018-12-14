package uk.ac.ed.inf.coinz

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
* This test is to test MenuActivity UI
* */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MenuActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(Menu::class.java)

    @Before
    fun preprocess(){
        resetDB()
    }
    @Test
    fun backToGame() {
        Espresso.onView(ViewMatchers.withId(R.id.btnBackToGame))         // withId(R.id.my_view) is a ViewMatcher
                .perform(ViewActions.click())                // click() is a ViewAction
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed())) // matches(isDisplayed()) is a ViewAssertion
    }

    @Test
    fun signOut() {
        Espresso.onView(ViewMatchers.withId(R.id.btnSignOut))         // withId(R.id.my_view) is a ViewMatcher
                .perform(ViewActions.click())                // click() is a ViewAction
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed())) // matches(isDisplayed()) is a ViewAssertion
    }
}