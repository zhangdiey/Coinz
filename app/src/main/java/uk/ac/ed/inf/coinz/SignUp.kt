package uk.ac.ed.inf.coinz

import android.os.Bundle
import android.app.Activity
import android.content.Intent

import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUp : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}
