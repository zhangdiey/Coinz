package uk.ac.ed.inf.coinz

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpTest {
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    val email = "test@test.com"
    val pwd = "123456"

    @Before
    // remove the account first
    fun deleteAccount(){
        mAuth = FirebaseAuth.getInstance()
        mAuth?.signInWithEmailAndPassword(email,pwd)
        mAuth?.currentUser?.delete()
    }
    @Test
    // sign up and then sign in
    fun signUpTest(){
        mAuth = FirebaseAuth.getInstance()
        mAuth?.createUserWithEmailAndPassword(email, pwd)
        mAuth?.signInWithEmailAndPassword(email,pwd)
        val user = mAuth?.currentUser
        Assert.assertEquals(null,user)
    }
}