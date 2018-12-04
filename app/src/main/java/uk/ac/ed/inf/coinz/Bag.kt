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
        setInfo() // get user's info from database and display it
        setItems() // get user's items including coins, gifts, and booster

        btnBack.setOnClickListener {
            val intent = Intent(this, GameMain::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, GameMain::class.java)
        startActivity(intent)
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
                        txtInfo.text = "Hello, $username!\nYou are at level $level and have $gold gold.\nClick on an item to use it."
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
        val coinRef = db?.collection("users")?.document(email!!)?.collection("coins") // reference to coins
        val coins = ArrayList<Coin>() // store all the coins
        val coinList = findViewById<ListView>(R.id.lvCoins)
        val coinListValue = ArrayList<String>() // store the displayed information
        coinRef?.get()
                ?.addOnSuccessListener {
                    if (it != null) {
                        for (document in it){
                            val coin:Coin = document.toObject(Coin::class.java)
                            coins.add(coin)
                            val property = JSONObject(coin.property)
                            val currency = property.get("currency")
                            val value = property.get("value")
                            coinListValue.add("Currency: $currency and Value: $value")
                        }
                    }
                    val coinAdapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,coinListValue)
                    coinList.adapter = coinAdapter
                }
        coinList.setOnItemClickListener { parent, view, position, id ->
            val coin:Coin = coins[position]
            val property = JSONObject(coin.property)
            val intent = Intent(this,StoreAndExchange::class.java)
            intent.putExtra("coinID",property.get("id").toString()) // pass coin ID to the next activity
            intent.putExtra("type","coins")
            startActivity(intent)
        }
        val giftRef = db?.collection("users")?.document(email!!)?.collection("gifts") // reference to gifts
        val gifts = ArrayList<Coin>() // store all the gifts
        val giftListValue = ArrayList<String>() // store the displayed information
        val giftList = findViewById<ListView>(R.id.lvGifts)
        giftRef?.get()
                ?.addOnSuccessListener {
                    if (it != null) {
                        for (document in it){
                            val coin:Coin = document.toObject(Coin::class.java)
                            gifts.add(coin)
                            val property = JSONObject(coin.property)
                            val currency = property.get("currency")
                            val value = property.get("value")
                            giftListValue.add("Currency: $currency and Value: $value")
                        }
                    }
                    val giftAdapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,giftListValue)
                    giftList.adapter = giftAdapter
                }
        giftList.setOnItemClickListener { parent, view, position, id ->
            val coin:Coin = gifts[position]
            val property = JSONObject(coin.property)
            val intent = Intent(this,StoreAndExchange::class.java)
            intent.putExtra("coinID",property.get("id").toString()) // pass coin ID to the next activity
            intent.putExtra("type","gifts")
            startActivity(intent)
        }
        val boosterRef = db?.collection("users")?.document(email!!)?.collection("boosters") // reference to boosters
        val boosters = ArrayList<Booster>() // store all the bossters
        val boostersListValue = ArrayList<String>() // store the displayed information
        val boosterList = findViewById<ListView>(R.id.lvBoosters)
        boosterRef?.get()
                ?.addOnSuccessListener {
                    if (it != null) {
                        for (document in it){
                            val booster:Booster = document.toObject(Booster::class.java)
                            boosters.add(booster)
                            val ratio = booster.ratio
                            val name = booster.name
                            boostersListValue.add("$name, use it to earn $ratio times of gold.")
                        }
                    }
                    val boosterAdapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,boostersListValue)
                    boosterList.adapter = boosterAdapter
                }
    }

    class User{
        var username = ""
        var level = 0
        var gold = 0.0
    }

    class Coin{
        var property = ""
    }
    class Booster{
        var ratio = ""
        var name = ""
    }
}
