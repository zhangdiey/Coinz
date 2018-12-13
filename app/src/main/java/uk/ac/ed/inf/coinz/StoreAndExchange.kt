package uk.ac.ed.inf.coinz

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_store_and_exchange.*
import org.json.JSONObject

class StoreAndExchange : AppCompatActivity() {
    private var coinID = ""
    private var coinValue = 0.0
    private var coinCurrency = ""
    private var goldValue = 0.0
    private var coin = Bag.Coin()
    private var db : FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null
    private var ratesHM = HashMap<String,Double>() // key:currency value:rate
    private val tag = "StoreAndExchange"
    private val preferencesFile = "MyPrefsFile"
    private var goldBoostRatio = 1.0 // the ratio when convert coins to gold. e.g. 2 means double.
    private var expBoostRatio = 1.0// the ratio the user will get for exp when storing coins. e.g. 2 means storing 1 coin = level up 2 level
    private var type = ""
    private var storeTimes = 0 // record how many times the user has stored coins

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_and_exchange)
        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
        db?.firestoreSettings = settings
        mAuth = FirebaseAuth.getInstance()

        // read exp boost ratio and gold boost ratio from pref file
        val fileSettings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        goldBoostRatio = fileSettings.getString("goldBoostRatio", "1").toDouble()
        expBoostRatio = fileSettings.getString("expBoostRatio", "1").toDouble()

        btnBack.setOnClickListener{
            val intent = Intent(this,Bag::class.java)
            startActivity(intent)
        }

        btnStore.setOnClickListener{
            // store the gold and delete the coin
            // if it is not a gift then check if the user has already stored his coins 25 times
            if (type == "gifts" || belowLimitedTimes()){
                store()
            } else {
                Toast.makeText(this,"You have already stored 25 coins today and cannot store any more coins!", Toast.LENGTH_LONG).show()
            }
        }

        btnExchange.setOnClickListener {
            // send the selected coin to another play by email
            // first check if the email is valid
            val rEmail = findViewById<EditText>(R.id.etxtEmail).text.toString() // recipient email
            val sEmail = mAuth?.currentUser?.email // sender email
            if (validEmail(rEmail)) {
                if (rEmail == sEmail) {
                    Toast.makeText(this,"You cannot send coins to yourself.",Toast.LENGTH_LONG).show()
                } else if (belowLimitedTimes()) {
                    // if the user has not stored the maximum amount (by default 25) of coins already, he/she cannot send coins to others.
                    Toast.makeText(this,"You cannot send coins because you can still deposit them.",Toast.LENGTH_LONG).show()
                }else {
                    send()
                }
            } else {
                Toast.makeText(this,"A valid email is required.",Toast.LENGTH_LONG).show()
            }
        }

        coinID = getIntent().getStringExtra("coinID")
        type = getIntent().getStringExtra("type")
        loadRates() // load rates from pref files
        loadCoin() // load information about the selected coin
    }

    private fun belowLimitedTimes():Boolean{
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        storeTimes = settings.getString("limitedStoreTimes", "0").toInt()
        if (storeTimes >= 25) {
            return false
        }
        return true
    }

    private fun store(){
        val user = mAuth?.currentUser
        val email = user?.email
        val userRef = db?.collection("users")?.document(email!!)
        var originalGold = 0.0
        var originalLevel = 0
        // update user's gold
        userRef?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(tag, "DocumentSnapshot data: " + document.data)
                        val user:Bag.User = document.toObject(Bag.User::class.java)!!
                        originalGold = user.gold
                        originalLevel = user.level
                        userRef.update("gold",originalGold + goldValue * goldBoostRatio)
                                .addOnCompleteListener{
                                    // level up
                                    userRef.update("level",originalLevel + expBoostRatio)
                                    // delete the coin
                                    val coinRef = db?.collection("users")?.document(email!!)?.collection(type)
                                    coinRef?.document("\"$coinID\"")?.delete()
                                    // update pref file if it is not a gift
                                    if (type == "coin") {
                                        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
                                        val editor = settings.edit()
                                        editor.putString("limitedStoreTimes",(storeTimes+1).toString())
                                        editor.apply()
                                    }
                                    Toast.makeText(this,"Successfully stored your coin!", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this,Bag::class.java)
                                    startActivity(intent)
                                }

                    } else {
                        Log.w(tag, "No such document")
                    }
                }

    }

    private fun send(){
        val txtEmail = findViewById<View>(R.id.etxtEmail) as EditText
        val rEmail = txtEmail.text.toString() // email of the recipient
        val user = mAuth?.currentUser
        val email = user?.email // email of the sender i.e. current user
        // check if the recipient email exist
        val reciIDRef = db?.collection("users")
        reciIDRef?.document(rEmail)?.get()
                ?.addOnSuccessListener { rDocument ->
                    if (rDocument.exists()){
                        val temp = HashMap<String,Any>() // store property
                        temp["property"] = coin.property
                        reciIDRef?.document(rEmail)?.collection("gifts")?.document("\"$coinID\"")
                                ?.set(temp)
                                ?.addOnCompleteListener {
                                    // delete the coin from sender
                                    val coinRef = db?.collection("users")?.document(email!!)?.collection(type)
                                    coinRef?.document("\"$coinID\"")?.delete()
                                    Log.d(tag, "$coinID has been added to database")
                                    Toast.makeText(this, "Coin sent to $rEmail!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this,Bag::class.java)
                                    startActivity(intent)
                                }
                                ?.addOnFailureListener {
                                    Log.d(tag, "Fail to add $coinID to database")
                                }
                    } else {
                        txtEmail.text.clear()
                        Toast.makeText(this, "$rEmail does not exits, please check it and try again!", Toast.LENGTH_SHORT).show()
                    }
                }

    }

    private fun loadRates(){
        // load the rates from pref file
        val txtInfo = findViewById<View>(R.id.txtRates) as TextView
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val rates = JSONObject(settings.getString("rates", ""))
        ratesHM["PENY"] = rates.getDouble("PENY")
        ratesHM["QUID"] = rates.getDouble("QUID")
        ratesHM["SHIL"] = rates.getDouble("SHIL")
        ratesHM["DOLR"] = rates.getDouble("DOLR")
        txtInfo.text = "Today's rates:\nPENY: ${ratesHM["PENY"]}\nQUID: ${ratesHM["QUID"]}\nSHIL: ${ratesHM["SHIL"]}\nDOLR: ${ratesHM["DOLR"]}"
    }

    private fun loadCoin(){
        // load the coin the user selected
        val user = mAuth?.currentUser
        val email = user?.email
        val txtInfo = findViewById<View>(R.id.txtInfo) as TextView
        val docRef = db?.collection("users")?.document(email!!)?.collection("$type")?.document("\"$coinID\"")
        docRef?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(tag, "DocumentSnapshot data: " + document.data)
                        coin = document.toObject(Bag.Coin::class.java)!!
                        val property = JSONObject(coin.property)
                        coinCurrency = property.get("currency").toString()
                        coinValue = property.get("value").toString().toDouble()
                        // calculate the gold value of the selected coin
                        when(coinCurrency){
                            "PENY" -> goldValue = coinValue * ratesHM["PENY"]!!
                            "QUID" -> goldValue = coinValue * ratesHM["QUID"]!!
                            "SHIL" -> goldValue = coinValue * ratesHM["SHIL"]!!
                            "DOLR" -> goldValue = coinValue * ratesHM["DOLR"]!!
                        }
                        txtInfo.text = "You have selected $coinValue $coinCurrency.\nIt can be converted to ${goldValue* goldBoostRatio} gold."
                    } else {
                        Log.w(tag, "No such document")
                    }
                }
    }

    private fun validEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}
