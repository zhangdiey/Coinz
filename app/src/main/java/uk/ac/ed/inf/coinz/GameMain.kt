package uk.ac.ed.inf.coinz

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_game_main.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class GameMain : AppCompatActivity() {

    private var myTask = DownloadFileTask(DownloadCompleteRunner,this)
    private val tag = "MainActivity"
    private var downloadDate = "" // Format: YYYY/MM/DD
    private val preferencesFile = "MyPrefsFile" // for storing preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_main)

        btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut();
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun downloadMap(date:String) {
        val today = java.util.Calendar.getInstance()
        Toast.makeText(this,"Downloading new map!", Toast.LENGTH_LONG).show()
        var url = "http://homepages.inf.ed.ac.uk/stg/coinz/$date/coinzmap.geojson"
        // download json
        myTask.execute(url)
    }

    fun saveMap() {
        val result : String? = DownloadCompleteRunner.result
        Toast.makeText(this,"Saving new map!", Toast.LENGTH_LONG).show()
        // write json to local storage
        val filename = "coinzmap.geojson"
        if (result == null) {
            // Handle exception
        } else {
            this.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(result.toByteArray())
            }
            Toast.makeText(this,"New map saved!", Toast.LENGTH_LONG).show()
        }
    }
    override fun onStart() {
        super.onStart()
        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "")
        // Write a message to ”logcat” (for debugging purposes)
        Log.d(tag, "[onStart] Recalled lastDownloadDate is '$ downloadDate'")
        // If today is not the last download day, download a new map
        val sdf = SimpleDateFormat("YYYY/MM/dd")
        val currentDate = sdf.format(Date())
        if (downloadDate == "" || currentDate != downloadDate){
            downloadMap(currentDate)
            downloadDate = currentDate
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "[onStop] Storing lastDownloadDate of$ downloadDate")
        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes.
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)
        // Apply the edits!
        editor.apply()
    }

    class DownloadFileTask(private val caller : DownloadCompleteListener, private val outer : GameMain):
            AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg urls: String): String = try {
            loadFileFromNetwork(urls[0])
        } catch (e: IOException) {
            "Unable to load content. Check your network connection"
        }

        private fun loadFileFromNetwork(urlString: String): String {
            val stream: InputStream = downloadUrl(urlString)
            // Read input from stream, build result as a string
            val br = BufferedReader(InputStreamReader(stream))
            val sb = StringBuilder()
            while (br.readLine() != null) {
                sb.append(br.readLine())
            }
            val result = sb.toString()
            br.close()
            return result
        }

        // Given a string representation of a URL, sets up a connection and gets an input stream.
        @Throws(IOException::class)
        private fun downloadUrl(urlString: String): InputStream {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            // Also available: HttpsURLConnection
            conn.readTimeout = 10000 // milliseconds
            conn.connectTimeout = 15000 // milliseconds
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect() // Starts the query
            return conn.inputStream
        }

        override fun onPostExecute(result: String)
        {
            super.onPostExecute(result)
            caller.downloadComplete(result)
            // save the map to local files
            outer.saveMap()
        }
    }

    interface DownloadCompleteListener {
        fun downloadComplete(result: String)
    }
    object DownloadCompleteRunner : DownloadCompleteListener
    {
        var result : String? = null
        override fun downloadComplete(result: String)
        {
            this.result = result
        }
    }
}
