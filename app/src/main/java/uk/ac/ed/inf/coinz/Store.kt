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
import kotlinx.android.synthetic.main.activity_store.*

class Store : AppCompatActivity() {
    private var db : FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null
    private val tag = "Shop"
    private var gold = 0.0 // amount of gold user currently has

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)
        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
        db?.firestoreSettings = settings
        mAuth = FirebaseAuth.getInstance()
        setInfo() // get user's gold from database and display it
        setItems() // get items including three types: gold, exp, and bank from database

        btnBack.setOnClickListener {
            val intent = Intent(this, GameMain::class.java)
            startActivity(intent)
        }
    }

    private fun setInfo(){
        // Load user's infor
        val user = mAuth?.currentUser
        val email = user?.email
        val txtInfo = findViewById<View>(R.id.txtInfo) as TextView
        val docRef = db?.collection("users")?.document(email!!)
        docRef?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(tag, "DocumentSnapshot data: " + document.data)
                        val user: Bag.User = document.toObject(Bag.User::class.java)!!
                        gold = user.gold
                        txtInfo.text = "Welcome! You have $gold gold.\nClick on an item to buy it."
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
        setGold() // load gold items
        setExp() // load exp items
        setOther() // load other items
    }

    private fun setGold(){
        // load gold items from database and add them to listview
        val boosterRef = db?.collection("gold_items") // reference to boosters
        val boosters = ArrayList<Bag.Booster>() // store all the boosters
        val boostersListValue = ArrayList<String>() // store the displayed information
        val boosterList = findViewById<ListView>(R.id.lvGB)
        boosterRef?.get()
                ?.addOnSuccessListener {
                    if (it != null) {
                        for (document in it){
                            val booster: Bag.Booster = document.toObject(Bag.Booster::class.java)
                            boosters.add(booster)
                            val cost = booster.cost
                            val ratio = booster.ratio
                            val name = booster.name
                            boostersListValue.add("Item Name: $name! Price: $cost gold \nTo earn $ratio times of gold.")
                        }
                    }
                    val boosterAdapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,boostersListValue)
                    boosterList.adapter = boosterAdapter
                }
        boosterList.setOnItemClickListener { parent, view, position, id ->
            val user = mAuth?.currentUser
            val email = user?.email // user email (ID)
            val booster = boosters[position] // get the booster selected
            // check the gold
            if (gold >= booster.cost) {
                db?.collection("users")
                        ?.document(email!!)
                        ?.collection("boosters")
                        ?.document(booster.name)
                        ?.set(booster)
                        ?.addOnCompleteListener {
                            // update gold (minus the cost)
                            val userRef = db?.collection("users")?.document(email!!)
                            userRef?.get()
                                    ?.addOnSuccessListener { document ->
                                        if (document != null) {
                                            userRef.update("gold", gold - booster.cost)
                                            // update user's gold
                                            setInfo()
                                        }
                                    }
                            Toast.makeText(this,"Item purchased! It has been delivered to your bag.", Toast.LENGTH_LONG).show()
                            Log.d(tag, "$email has been added to database")
                        }
                        ?.addOnFailureListener {
                            Log.d(tag, "Failed to add booster under $email to database")
                        }
            } else {
                Toast.makeText(this,"Insufficient gold.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setExp(){
        // similar to setGold
        val boosterRef = db?.collection("exp_items") // reference to boosters
        val boosters = ArrayList<Bag.Booster>() // store all the boosters
        val boostersListValue = ArrayList<String>() // store the displayed information
        val boosterList = findViewById<ListView>(R.id.lvEB)
        boosterRef?.get()
                ?.addOnSuccessListener {
                    if (it != null) {
                        for (document in it){
                            val booster: Bag.Booster = document.toObject(Bag.Booster::class.java)
                            boosters.add(booster)
                            val cost = booster.cost
                            val ratio = booster.ratio
                            val name = booster.name
                            boostersListValue.add("Item Name: $name! Price: $cost gold \nTo earn $ratio times of experience.")
                        }
                    }
                    val boosterAdapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,boostersListValue)
                    boosterList.adapter = boosterAdapter
                }
        boosterList.setOnItemClickListener { parent, view, position, id ->
            val user = mAuth?.currentUser
            val email = user?.email // user email (ID)
            val booster = boosters[position] // get the booster selected
            // check the gold
            if (gold >= booster.cost) {
                db?.collection("users")
                        ?.document(email!!)
                        ?.collection("boosters")
                        ?.document(booster.name)
                        ?.set(booster)
                        ?.addOnCompleteListener {
                            // update gold (minus the cost)
                            val userRef = db?.collection("users")?.document(email!!)
                            userRef?.get()
                                    ?.addOnSuccessListener { document ->
                                        if (document != null) {
                                            userRef.update("gold", gold - booster.cost)
                                            // update user's gold
                                            setInfo()
                                        }
                                    }
                            Toast.makeText(this,"Item purchased! It has been delivered to your bag.", Toast.LENGTH_LONG).show()
                            Log.d(tag, "$email has been added to database")
                        }
                        ?.addOnFailureListener {
                            Log.d(tag, "Failed to add booster under $email to database")
                        }
            } else {
                Toast.makeText(this,"Insufficient gold.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setOther(){
        // similar to setOther
        val boosterRef = db?.collection("bank_items") // reference to boosters
        val boosters = ArrayList<Bag.Booster>() // store all the boosters
        val boostersListValue = ArrayList<String>() // store the displayed information
        val boosterList = findViewById<ListView>(R.id.lvOI)
        boosterRef?.get()
                ?.addOnSuccessListener {
                    if (it != null) {
                        for (document in it){
                            val booster: Bag.Booster = document.toObject(Bag.Booster::class.java)
                            boosters.add(booster)
                            val amount = booster.amount
                            val cost = booster.cost
                            val name = booster.name
                            boostersListValue.add("Item name: $name! Price: $cost gold \nTo store extra $amount to bank.")
                        }
                    }
                    val boosterAdapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,boostersListValue)
                    boosterList.adapter = boosterAdapter
                }
        boosterList.setOnItemClickListener { parent, view, position, id ->
            val user = mAuth?.currentUser
            val email = user?.email // user email (ID)
            val booster = boosters[position] // get the booster selected
            // check the gold
            if (gold >= booster.cost) {
                db?.collection("users")
                        ?.document(email!!)
                        ?.collection("boosters")
                        ?.document(booster.name)
                        ?.set(booster)
                        ?.addOnCompleteListener {
                            // update gold (minus the cost)
                            val userRef = db?.collection("users")?.document(email!!)
                            userRef?.get()
                                    ?.addOnSuccessListener { document ->
                                        if (document != null) {
                                            userRef.update("gold", gold - booster.cost)
                                            // update user's gold
                                            setInfo()
                                        }
                                    }
                            Toast.makeText(this,"Item purchased! It has been delivered to your bag.", Toast.LENGTH_LONG).show()
                            Log.d(tag, "$email has been added to database")
                        }
                        ?.addOnFailureListener {
                            Log.d(tag, "Failed to add booster under $email to database")
                        }
            } else {
                Toast.makeText(this,"Insufficient gold.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
