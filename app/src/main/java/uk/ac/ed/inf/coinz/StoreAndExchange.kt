package uk.ac.ed.inf.coinz

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
    private var db : FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null
    private var ratesHM = HashMap<String,Double>() // key:currency value:rate
    private val tag = "StoreAndExchange"
    private val preferencesFile = "MyPrefsFile"
    private var boostRatio = 1 // the ratio when convert coins to gold. e.g. 2 means double.
    private var type = ""
    private var storeTimes = 0 // record how many times the user has stored coins

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_and_exchange)
        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
        db?.firestoreSettings = settings
        mAuth = FirebaseAuth.getInstance()

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
        val goldRef = db?.collection("users")?.document(email!!)
        var originalGold = 0.0
        // update user's gold
        goldRef?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(tag, "DocumentSnapshot data: " + document.data)
                        val user:Bag.User = document.toObject(Bag.User::class.java)!!
                        originalGold = user.gold
                        goldRef.update("gold",originalGold + goldValue * boostRatio)
                                .addOnCompleteListener{
                                    // delete the coin
                                    val coinRef = db?.collection("users")?.document(email!!)?.collection(type)
                                    coinRef?.document("\"$coinID\"")?.delete()
                                    // update pref file
                                    val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
                                    val editor = settings.edit()
                                    editor.putString("limitedStoreTimes",(storeTimes+1).toString())
                                    editor.apply()
                                    Toast.makeText(this,"Successfully stored your coin!", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this,Bag::class.java)
                                    startActivity(intent)
                                }

                    } else {
                        Log.w(tag, "No such document")
                    }
                }

    }

    private fun loadRates(){
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
        val user = mAuth?.currentUser
        val email = user?.email
        val txtInfo = findViewById<View>(R.id.txtInfo) as TextView
        val docRef = db?.collection("users")?.document(email!!)?.collection("coins")?.document("\"$coinID\"")
        docRef?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(tag, "DocumentSnapshot data: " + document.data)
                        var coin : Bag.Coin = document.toObject(Bag.Coin::class.java)!!
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
                        txtInfo.text = "You have selected $coinValue $coinCurrency.\nIt can be converted to $goldValue gold."
                    } else {
                        Log.w(tag, "No such document")
                    }
                }
    }

}