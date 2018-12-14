package uk.ac.ed.inf.coinz

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
* This test is to test Game UI
* */
@RunWith(AndroidJUnit4::class)
@LargeTest
class GameTest {

    @get:Rule
    val activityRule = ActivityTestRule(GameMain::class.java)

    @Before
    fun preprocess(){
        resetDB()
    }
    @Before
    fun logIn(){
        val mAuth = FirebaseAuth.getInstance()
        Assert.assertNotEquals(null,mAuth)
        mAuth?.signInWithEmailAndPassword("test@account.com","123456")
    }
    @Test
    fun bag() {
        onView(withId(R.id.btnBag))         // withId(R.id.my_view) is a ViewMatcher
                .perform(click())                // click() is a ViewAction
                .check(matches(isDisplayed())) // matches(isDisplayed()) is a ViewAssertion
    }
    @Test
    fun shop() {
        onView(withId(R.id.btnShop))         // withId(R.id.my_view) is a ViewMatcher
                .perform(click())                // click() is a ViewAction
                .check(matches(isDisplayed())) // matches(isDisplayed()) is a ViewAssertion
    }
    @Test
    fun menu() {
        onView(withId(R.id.btnMenu))         // withId(R.id.my_view) is a ViewMatcher
                .perform(click())                // click() is a ViewAction
                .check(matches(isDisplayed())) // matches(isDisplayed()) is a ViewAssertion
    }
    @Test
    fun center() {
        onView(withId(R.id.btnCenter))         // withId(R.id.my_view) is a ViewMatcher
                .perform(click())                // click() is a ViewAction
                .check(matches(isDisplayed())) // matches(isDisplayed()) is a ViewAssertion
    }
}

