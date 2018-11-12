package uk.ac.ed.inf.coinz

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUp : Activity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        mAuth = FirebaseAuth.getInstance()

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnSignUp.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val txtName = findViewById<View>(R.id.etxtName) as EditText
        val txtEmail = findViewById<View>(R.id.etxtEmail) as EditText
        val txtPwd = findViewById<View>(R.id.etxtPwd) as EditText

        var name = txtName.text.toString()
        var email = txtEmail.text.toString()
        var pwd = txtPwd.text.toString()

        if (!name.isEmpty() && validEmail(email) && validPwd(pwd)) {
            mAuth?.createUserWithEmailAndPassword(email, pwd)
                    ?.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this,"Welcome to CoinZ.",Toast.LENGTH_LONG).show()
                            val intent = Intent(this, GameMain::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this,"This email is already taken.",Toast.LENGTH_LONG).show()
                        }
                    }
        } else if (!validEmail(email)){
            Toast.makeText(this,"A valid email is required.",Toast.LENGTH_LONG).show()
        } else if (!validPwd(pwd)){
            Toast.makeText(this,"Password must be at least 6 characters.",Toast.LENGTH_LONG).show()
        } else if (name.isEmpty()){
            Toast.makeText(this,"A valid username is required.",Toast.LENGTH_LONG).show()
        }
    }

    fun validEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    fun validPwd(pwd: String): Boolean {
        return pwd.length>=6
    }
}
