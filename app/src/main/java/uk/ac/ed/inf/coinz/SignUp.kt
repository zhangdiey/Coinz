package uk.ac.ed.inf.coinz

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUp : Activity() {

    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    private val tag = "SignUp"

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

        db = FirebaseFirestore.getInstance()
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
        db?.firestoreSettings = settings
    }

    private fun register() {
        val txtName = findViewById<View>(R.id.etxtName) as EditText
        val txtEmail = findViewById<View>(R.id.etxtEmail) as EditText
        val txtPwd = findViewById<View>(R.id.etxtPwd) as EditText

        val name = txtName.text.toString()
        val email = txtEmail.text.toString()
        val pwd = txtPwd.text.toString()

        if (!name.isEmpty() && validEmail(email) && validPwd(pwd)) {
            mAuth?.createUserWithEmailAndPassword(email, pwd)
                    ?.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            addToDB(name,email)
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

    private fun addToDB(name:String,email:String){
        val user= HashMap<String,Any>()
        user.put("username",name)
        user.put("gold",0)
        user.put("level",0)
        db?.collection("users")
                ?.document(email)
                ?.set(user)
                ?.addOnCompleteListener {
                    Log.d(tag, "$email has been added to database")
        }
                ?.addOnFailureListener {
                    Log.d(tag, "Failed to add $email to database")
        }
    }

    // check if email is valid
    private fun validEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // check is password is more than 6 in length
    private fun validPwd(pwd: String): Boolean {
        return pwd.length>=6
    }
}
