package uk.ac.ed.inf.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_bag.*

class Bag : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bag)

        btnBack.setOnClickListener {
            val intent = Intent(this, GameMain::class.java)
            startActivity(intent)
        }
    }
}
