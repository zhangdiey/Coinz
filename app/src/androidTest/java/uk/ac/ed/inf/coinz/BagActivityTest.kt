package uk.ac.ed.inf.coinz

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
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
* This test is to test BagActivity UI
* */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BagActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(Bag::class.java)

    @Before
    fun preprocess(){
        resetDB()
    }
    @Before
    fun logIn(){
        val mAuth = FirebaseAuth.getInstance()
        mAuth?.signInWithEmailAndPassword("test@account.com","123456")
        Assert.assertEquals("test@account.com",mAuth.currentUser?.email)
    }
    @Test
    fun backToGame() {
        Espresso.onView(ViewMatchers.withId(R.id.btnBackToGame))         // withId(R.id.my_view) is a ViewMatcher
                .perform(ViewActions.click())                // click() is a ViewAction
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed())) // matches(isDisplayed()) is a ViewAssertion
    }

    @Test
    fun click() {
        Espresso.onView(ViewMatchers.withId(R.id.lvCoins))         // withId(R.id.my_view) is a ViewMatcher
                .perform(ViewActions.click())                // click() is a ViewAction
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed())) // matches(isDisplayed()) is a ViewAssertion
    }
}