package uk.ac.ed.inf.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_bag.*
import org.json.JSONObject

class Bag : AppCompatActivity() {
    private var db : FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null
    private val tag = "Bag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bag)
        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
        db?.firestoreSettings = settings
        mAuth = FirebaseAuth.getInstance()
        Toast.makeText(this,"Loading your information...", Toast.LENGTH_LONG).show()
        setInfo() // get user's info from database and display it
        setItems() // get user's items including coins, gifts, and booster

        btnBack.setOnClickListener {
            val intent = Intent(this, GameMain::class.java)
            startActivity(intent)
        }
    }

    private fun setInfo(){
        val user = mAuth?.currentUser
        val email = user?.email
        val txtInfo = findViewById<View>(R.id.txtInfo) as TextView
        val docRef = db?.collection("users")?.document(email!!)
        docRef?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(tag, "DocumentSnapshot data: " + document.data)
                        val user:User = document.toObject(User::class.java)!!
                        val username = user.username
                        val level = user.level
                        val gold = user.gold
                        txtInfo.text = "$email \nHello, $username!\nYou are at level $level and you have $gold gold in your bank."
                    } else {
                        Toast.makeText(this,"Fail to load user info.", Toast.LENGTH_LONG).show()
                        Log.w(tag, "No such document")
                    }
                }
                ?.addOnFailureListener { exception ->
                    Log.w(tag, "get failed with ", exception)
                }
    }

    private fun setItems(){
        val user = mAuth?.currentUser
        val email = user?.email
        val coinRef = db?.collection("users")?.document(email!!)?.collection("coins")
        val coins = ArrayList<String>()
        val coinList = findViewById<ListView>(R.id.lvCoins)
        coinRef?.get()
                ?.addOnSuccessListener {
                    if (it != null) {
                        for (document in it){
                            val coin:Coin = document.toObject(Coin::class.java)
                            val property = JSONObject(coin.property)
                            val id = property.get("id")
                            val currency = property.get("currency")
                            val value = property.get("value")
                            coins.add("id: $id \ncurrency: $currency and value: $value")
                        }
                    }
                    val coinAdapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,coins)
                    coinList.adapter = coinAdapter
                }
        val giftRef = db?.collection("users")?.document(email!!)?.collection("gifts")
        val gifts = ArrayList<String>()
        val giftList = findViewById<ListView>(R.id.lvGifts)
        giftRef?.get()
                ?.addOnSuccessListener {
                    if (it != null) {
                        for (document in it){
                            val coin:Coin = document.toObject(Coin::class.java)
                            val property = JSONObject(coin.property)
                            val id = property.get("id")
                            val currency = property.get("currency")
                            val value = property.get("value")
                            gifts.add("id: $id \ncurrency: $currency and value: $value")
                        }
                    }
                    val giftAdapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,gifts)
                    giftList.adapter = giftAdapter
                }
    }

    class User{
        var username = ""
        var level = 0
        var gold = 0
    }

    class Coin{
        var property = ""
    }

}
