package com.example.greenhouse

import FirestoreWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.checkSelfPermission
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt
import com.google.android.gms.location.LocationServices
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var recyclerView: RecyclerView
    private lateinit var greenhouseList: MutableList<Greenhouse>
    private lateinit var usernameTextView: TextView
    private lateinit var buttonDashboard: Button
    private lateinit var buttonControl: Button
    private lateinit var buttonSettings: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val weatherScope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var weatherIcon: ImageView
    private lateinit var temperatureText: TextView
    private lateinit var conditionText: TextView
    private lateinit var precipitationText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var weatherContainer: LinearLayout


    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
        private const val MET_BASE_URL = "https://api.met.no/weatherapi/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scheduleFirestoreCheck()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            initializeUI()
            setupMap()
            setupButtons()
            initializeRecyclerView()
            fetchGreenhouses(currentUser.uid)
            checkLocationPermission()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun initializeUI() {
        usernameTextView = findViewById(R.id.username_text_view)
        weatherIcon = findViewById(R.id.weather_icon)
        temperatureText = findViewById(R.id.temperature_text)
        conditionText = findViewById(R.id.condition_text)
        precipitationText = findViewById(R.id.precipitation_text)
        buttonDashboard = findViewById(R.id.button_dashboard)
        buttonControl = findViewById(R.id.button_control)
        buttonSettings = findViewById(R.id.button_settings)
        weatherContainer = findViewById(R.id.weather_details_container)

        weatherContainer.setOnClickListener {
            startActivity(Intent(this, WeatherForecastActivity::class.java))
        }
    }

    private fun setupMap() {
        val osmdroidPrefs = getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().load(applicationContext, osmdroidPrefs)

        mapView = findViewById(R.id.osm_map_view)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(13.5)
        val defaultLocation = GeoPoint(44.59, 18.68)
        mapController.setCenter(defaultLocation)
    }

    private fun initializeRecyclerView() {
        greenhouseList = mutableListOf()
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = GreenhouseAdapter(greenhouseList) { greenhouse ->
            val intent = Intent(this, GreenhouseDetailsActivity::class.java)
            intent.putExtra("greenhouseId", greenhouse.name)
            startActivity(intent)
        }
    }


    private fun setupButtons() {
        buttonDashboard.setOnClickListener {
            highlightButton(buttonDashboard)
        }

        buttonControl.setOnClickListener {
            startActivity(Intent(this, ControlActivity::class.java))
            highlightButton(buttonControl)
        }

        buttonSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            highlightButton(buttonSettings)
        }
    }

    private fun fetchGreenhouses(userId: String) {
        db.collection("users").document(userId).collection("greenhouses")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firestore", "Error fetching greenhouses", error)
                    return@addSnapshotListener
                }

                snapshots?.let { documents ->
                    greenhouseList.clear()
                    val markerOverlays = mapView.overlays.filterIsInstance<Marker>().toMutableList()
                    mapView.overlays.removeAll(markerOverlays)

                    for (document in documents) {
                        try {
                            val greenhouse = Greenhouse(
                                name = document.getString("name") ?: "",
                                humidity = document.getString("humidity") ?: "0",
                                temperature = document.getString("temperature") ?: "0",
                                acidity = document.getString("acidity") ?: "0",
                                imageResId = R.drawable.greenhouse
                            )
                            greenhouseList.add(greenhouse)
                            document.getGeoPoint("location")?.let { geoPoint ->
                                Log.d("Firestore", "Greenhouse ${document.getString("name")} has location: ${geoPoint.latitude}, ${geoPoint.longitude}")
                                addMarkerToMap(GeoPoint(geoPoint.latitude, geoPoint.longitude), greenhouse.name)
                            } ?: Log.w("Firestore", "Greenhouse ${document.getString("name")} is missing location data!")


                            document.getGeoPoint("location")?.let { geoPoint ->
                                addMarkerToMap(

                                    GeoPoint(geoPoint.latitude, geoPoint.longitude),
                                    greenhouse.name
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("Firestore", "Error parsing greenhouse document", e)
                        }
                    }
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
    }

    private fun scheduleFirestoreCheck() {
        val workRequest = PeriodicWorkRequestBuilder<FirestoreWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun checkLocationPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                }
            }
        }
    }

    private fun getCurrentLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    fetchWeatherData(it.latitude, it.longitude)
                }
            }
        }
    }

    private fun addMarkerToMap(point: GeoPoint, title: String) {
        Log.d("MapDebug", "Adding marker: $title at ${point.latitude}, ${point.longitude}")

        val marker = Marker(mapView)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        runOnUiThread {
            mapView.overlays.add(marker)
            mapView.invalidate()
        }
    }


    private fun highlightButton(activeButton: Button) {
        buttonDashboard.setBackgroundColor(resources.getColor(android.R.color.transparent))
        buttonControl.setBackgroundColor(resources.getColor(android.R.color.transparent))
        buttonSettings.setBackgroundColor(resources.getColor(android.R.color.transparent))

        activeButton.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
    }

    private fun fetchWeatherData(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.met.no/weatherapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherService::class.java)

        weatherScope.launch {
            try {
                val response = weatherService.getWeather(lat, lon)

                val latestData = response.properties.timeseries.firstOrNull()?.data

                if (latestData != null) {
                    updateWeatherUI(
                        WeatherData(
                            temperature = latestData.instant.details.air_temperature,
                            condition = latestData.next_1_hours?.summary?.symbol_code ?: "unknown",
                            precipitation = latestData.instant.details.precipitation_amount
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("Weather", "Error fetching weather", e)
            }
        }
    }



    private fun updateWeatherUI(weatherData: WeatherData) {
        temperatureText.text = "${weatherData.temperature.roundToInt()}°C"
        conditionText.text = formatWeatherCondition(weatherData.condition)
        precipitationText.text = "${weatherData.precipitation ?: 0.0} mm"

        weatherIcon.setImageResource(getWeatherIcon(weatherData.condition))
    }

    private fun formatWeatherCondition(condition: String): String {
        return condition.replace("_", " ").capitalize()
    }

    private fun getWeatherIcon(condition: String): Int {
        return when (condition) {
            "clearsky_day" -> R.drawable.sunny
            "partlycloudy_day" -> R.drawable.cloudy
            "cloudy" -> R.drawable.very_cloudy
            "rain" -> R.drawable.rain
            "snow" -> R.drawable.snow
            else -> R.drawable.cloudy
        }
    }
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        weatherScope.cancel()
    }
}