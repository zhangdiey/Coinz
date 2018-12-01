package uk.ac.ed.inf.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_menu.*

class Menu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnBackToGame.setOnClickListener {
            val intent = Intent(this, GameMain::class.java)
            startActivity(intent)
        }
    }
}
