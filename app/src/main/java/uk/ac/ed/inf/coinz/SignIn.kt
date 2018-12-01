package uk.ac.ed.inf.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignIn : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        mAuth = FirebaseAuth.getInstance()

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnSignIn.setOnClickListener {
            signin()
        }
    }

    private fun signin(){
        val txtEmail = findViewById<View>(R.id.etxtEmail) as EditText
        val txtPwd = findViewById<View>(R.id.etxtPwd) as EditText

        val email = txtEmail.text.toString()
        val pwd = txtPwd.text.toString()
        if (validEmail(email) && validPwd(pwd)){
            mAuth?.signInWithEmailAndPassword(email,pwd)
                    ?.addOnCompleteListener(this) {task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this,"Welcome Back!", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, GameMain::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this,"Please check your email and password.", Toast.LENGTH_LONG).show()
                        }
                    }
        } else if (!validEmail(email)){
            Toast.makeText(this,"A valid email is required.",Toast.LENGTH_LONG).show()
        } else if (!validPwd(pwd)) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_LONG).show()
        }
    }

    private fun validEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validPwd(pwd: String): Boolean {
        return pwd.length>=6
    }
}
