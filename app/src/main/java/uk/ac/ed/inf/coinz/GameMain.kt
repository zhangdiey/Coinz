package uk.ac.ed.inf.coinz

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import kotlinx.android.synthetic.main.activity_game_main.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject
import kotlin.collections.HashMap

class GameMain : AppCompatActivity(), OnMapReadyCallback, LocationEngineListener,PermissionsListener {
    private var myTask = DownloadFileTask(DownloadCompleteRunner,this)
    private val tag = "MainActivity"
    private var downloadDate = "" // Format: YYYY/MM/DD
    private val preferencesFile = "MyPrefsFile" // for storing preferences
    private var mapView: MapView? = null
    private var map: MapboxMap? = null
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var locationLayerPlugin: LocationLayerPlugin
    private lateinit var originLocation: Location
    private val filename = "coinzmap.geojson"
    private var backButtonCount = 0 // press back twice to exit
    private var markers = mutableListOf<Marker>() // all the markers
    private var markers2features = hashMapOf<Marker,Feature>() // key/value for marker/property
    private var db : FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_main)
        Mapbox.getInstance(applicationContext,getString(R.string.access_token))
        mapView = findViewById(R.id.mapboxMapView)
        mapView?.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mapView?.getMapAsync(this)
        // Use com.google.firebase.Timestamp objects instead of java.util.Date objects
        val settings = FirebaseFirestoreSettings.Builder().setTimestampsInSnapshotsEnabled(true).build()
        db?.firestoreSettings = settings
        realtimeUpdateListener()

        btnMenu.setOnClickListener {
            val intent = Intent(this, Menu::class.java)
            startActivity(intent)
        }

        btnBag.setOnClickListener {
            val intent = Intent(this,Bag::class.java)
            startActivity(intent)
        }

        btnShop.setOnClickListener {
            val intent = Intent(this,Store::class.java)
            startActivity(intent)
        }

        btnCenter.setOnClickListener {
            centerMe()
        }
    }

    private fun realtimeUpdateListener() {
        // Not going to implement for this project
    }

    override fun onBackPressed() {
        // press twice to exit (but not sign out)
        if (backButtonCount >= 1) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } else {
            Toast.makeText(this, "Press the back button again to exit coinz.", Toast.LENGTH_SHORT).show()
            backButtonCount++
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap
        // Set user interface options
        map?.uiSettings?.isCompassEnabled = true
        map?.uiSettings?.isZoomControlsEnabled = true
        enableLocation()
        renderMarker()
    }

    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // after granted permission
            Log.d(tag, "Permissions are granted")
            initializeLocationEngine()
            initializeLocationLayer()
        }else {
            Log.d(tag, "Permissions are not granted")
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initializeLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine.apply{
            interval = 5000 // preferably every 5 seconds
            fastestInterval = 1000 // at most every second
            priority = LocationEnginePriority.HIGH_ACCURACY
            activate()
        }
        locationEngine.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine.activate()

        val lastLocation : Location? = locationEngine.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        } else {
            locationEngine.addLocationEngineListener(this)
        }
    }

    private fun centerMe(){
        // set my location in the center
        if (locationEngine.lastLocation != null) {
            setCameraPosition(locationEngine.lastLocation)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initializeLocationLayer() {
        locationLayerPlugin = LocationLayerPlugin(mapView!!,map!!,locationEngine)
        locationLayerPlugin.setLocationLayerEnabled(true)
        locationLayerPlugin.cameraMode = CameraMode.TRACKING
        locationLayerPlugin.renderMode = RenderMode.NORMAL
    }

    private fun setCameraPosition(location: Location){
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude),15.0))
    }

    override fun onLocationChanged(location: Location?) {
        // center my location if moved
        if (location != null) {
            originLocation = location
            setCameraPosition(location)
            collect()
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onConnected() {
        locationEngine.requestLocationUpdates()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this,"Please allow us to use your location to play coinz!", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocation()
        } else {
            Toast.makeText(this,"Please allow us to use your location to play coinz!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun downloadMap(date:String) {
        Toast.makeText(this,"Downloading new map!", Toast.LENGTH_LONG).show()
        val url = "http://homepages.inf.ed.ac.uk/stg/coinz/$date/coinzmap.geojson"
        // download json
        myTask.execute(url)
    }

    private fun saveMap() {
        val result : String? = DownloadCompleteRunner.result
        var rates = ""
        Toast.makeText(this,"Saving new map!", Toast.LENGTH_LONG).show()
        // write json to local storage
        if (result == null) {
            // Handle exception
        } else {
            this.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(result.toByteArray())
            }
            Toast.makeText(this,"New map saved!", Toast.LENGTH_LONG).show()
        }
        Log.d(tag, "[saveMap] Storing lastDownloadDate of$ downloadDate")
        // All objects are from android.context.Context
        this.openFileInput(filename).use {
            val result = JSONObject(streamToString(it))
            rates = result.get("rates").toString()
        }
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes.
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)
        // update the rates
        editor.putString("rates", rates)
        // reset the store time
        editor.putString("limitedStoreTimes","0")
        // reset exp booster
        editor.putString("expBoostRatio","1")
        // reset gold booster
        editor.putString("goldBoostRatio","1")
        // Apply the edits!
        editor.apply()
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
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
            // remove all the coins from previous days
            val user = mAuth?.currentUser
            val email = user?.email
            val coinRef = db?.collection("users")?.document(email!!)?.collection("coins")
            deleteCollection(coinRef!!,50)
        }
    }

    // remove the collection given the reference
    private fun deleteCollection(collection: CollectionReference, batchSize: Int) {
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

    private fun renderMarker(){
        // use the coin icon
        val drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.coin_icon, null)
        val icon = IconFactory.getInstance(this).fromBitmap(drawableToBitmap(drawable!!))
        this.openFileInput(filename).use {
            val result = streamToString(it)
            val featureCollection = FeatureCollection.fromJson(result)
            val features = featureCollection.features()
            if (features == null) {
                // no features left
                Toast.makeText(this,"You have collected all the coins!", Toast.LENGTH_LONG).show()
            } else {
                for (f in features) {
                    if (f.geometry() is Point) {
                        val point = f.geometry()!! as Point
                        val marker = map?.addMarker(MarkerOptions().icon(icon).position(LatLng(point.latitude(), point.longitude())))!!
                        saveMarker(marker,f)
                    }
                }
            }
        }
    }

    // convert a drawable to bitmap for icon
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        var bitmap: Bitmap? = null

        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return Bitmap.createScaledBitmap(drawable.bitmap, 25, 25, false)
            }
        }

        if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap!!)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return Bitmap.createScaledBitmap(bitmap, 25, 25, false)
    }

    private fun saveMarker(marker:Marker, feature: Feature){
        markers.add(marker)
        markers2features[marker] = feature
    }

    private fun collect(){
        if (locationEngine.lastLocation != null) {
            val currentLocation = locationEngine.lastLocation
            val curLat = currentLocation.latitude
            val curLng = currentLocation.longitude
            val iterate = markers.listIterator()
            // looking for coins within 25 meters
            while (iterate.hasNext()) {
                val marker = iterate.next()
                if (marker.position.distanceTo(LatLng(curLat,curLng)) <= 25) {
                    // remove marker and add the coin to database
                    map?.removeMarker(marker)
                    upload(marker)
                    iterate.remove()
                }
            }
            // update coinzmap.geojson file
            this.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(FeatureCollection.fromFeatures(markers2features.values.toMutableList()).toJson().toByteArray())
            }
        }
    }

    private fun upload(marker: Marker){
        // given a marker, upload the corresponding properties to firebase then remove it from markers2features and coinzmap.geojson
        val f = markers2features[marker]
        val p = f?.properties()
        val user = mAuth?.currentUser
        val email = user?.email
        val id = p?.get("id").toString() // id of the coin
        val temp = HashMap<String,Any>() // store property
        temp["property"] = p.toString()
        // store the collected coin in firestore
        db?.collection("users")
                ?.document(email!!)
                ?.collection("coins")
                ?.document(id)
                ?.set(temp)
                ?.addOnCompleteListener {
                    Log.d(tag, "$id has been added to database")
                    Toast.makeText(this, "Coin collected!", Toast.LENGTH_SHORT).show()
                }
                ?.addOnFailureListener {
                    Log.d(tag, "Fail to add $id to database")
                }
        markers2features.remove(marker)
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        // deactivate LocationEngine
        locationEngine.deactivate()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        if (outState != null) {
            mapView?.onSaveInstanceState(outState)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onStop() {
        super.onStop()
        // stop LocationEngine and LocationLayerPlugin
        locationEngine.removeLocationUpdates()
        locationLayerPlugin.onStop()
        Log.d(tag, "[onStop] Storing lastDownloadDate of$ downloadDate")
        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes.
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)
        // Apply the edits!
        editor.apply()
    }

    private fun streamToString(stream: InputStream):String {
        val br = BufferedReader(InputStreamReader(stream))
        val sb = StringBuilder()
        var line = br.readLine()
        while (line != null) {
            sb.append(line)
            sb.append("\n")
            line = br.readLine()
        }
        val result = sb.toString()
        br.close()
        return result
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
            val result = outer.streamToString(stream)
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
