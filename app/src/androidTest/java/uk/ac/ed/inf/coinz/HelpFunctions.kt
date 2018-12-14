package uk.ac.ed.inf.coinz

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

// this file contains functions used for various tests

fun resetDB(){
    val mAuth: FirebaseAuth? = FirebaseAuth.getInstance()
    val db: FirebaseFirestore? = FirebaseFirestore.getInstance()
    // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
    val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
    db?.firestoreSettings = settings
    // login to the test account and set the default value
    val email = "test@account.com"
    val pwd = "123456"
    val name = "test123"
    mAuth?.signInWithEmailAndPassword(email,pwd)
    // default value
    val user = HashMap<String,Any>()
    user.put("username",name)
    user.put("gold",10000) // 10000 gold so that we can test the shop function
    user.put("level",0)
    val accountRef = db?.collection("users")?.document(email)?.update(user)
    // remove all the coins, gifts, and boosters
    val coinRef = db?.collection("users")?.document(email)?.collection("coins")
    deleteCollection(coinRef!!,50)
    val giftRef = db.collection("users").document(email).collection("gifts")
    deleteCollection(giftRef,50)
    val boosterRef = db.collection("users").document(email).collection("boosters")
    deleteCollection(boosterRef,50)
}

fun deleteCollection(collection: CollectionReference, batchSize: Int) {
    try {
        // Retrieve a small batch of documents to avoid out-of-memory errors/
        var deleted = 0
        collection
                .limit(batchSize.toLong())
                .get()
                .addOnCompleteListener {
                    for (document in it.result!!.documents) {
                        document.reference.delete()
                        ++deleted
                    }
                    if (deleted >= batchSize) {
                        // retrieve and delete another batch
                        deleteCollection(collection, batchSize)
                    }
                }
    } catch (e: Exception) {
        Log.e("Error deleting collection : ", e.message)
    }
}