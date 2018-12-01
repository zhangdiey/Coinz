package uk.ac.ed.inf.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
        var user = mAuth?.getCurrentUser()
        if (user != null) {
            // User is signed in
            Toast.makeText(this,"Welcome Back!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, GameMain::class.java)
            startActivity(intent)
        } else {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this,"Please Join Us!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnSignIn.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }
}
