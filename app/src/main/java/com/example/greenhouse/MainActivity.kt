package com.example.greenhouse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var recyclerView: RecyclerView
    private lateinit var greenhouseList: List<Greenhouse>
    private lateinit var usernameTextView: TextView
    private lateinit var buttonDashboard: Button
    private lateinit var buttonControl: Button
    private lateinit var buttonSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        setContentView(R.layout.activity_main)

        if (isLoggedIn) {

            usernameTextView = findViewById(R.id.username_text_view)
            buttonDashboard = findViewById(R.id.button_dashboard)
            buttonControl = findViewById(R.id.button_control)
            buttonSettings = findViewById(R.id.button_settings)

            val username = sharedPreferences.getString("username", "User")
            usernameTextView.text = "Hello, $username!"

            val osmdroidPrefs = getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
            Configuration.getInstance().load(applicationContext, osmdroidPrefs)
            mapView = findViewById(R.id.osm_map_view)
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.setBuiltInZoomControls(true)
            mapView.setMultiTouchControls(true)
            val mapController = mapView.controller
            mapController.setZoom(13.5)
            val point1 = GeoPoint(44.59, 18.68)
            mapController.setCenter(point1)
            addMarkerToMap(point1, "Greenhouse 1")
            val point2 = GeoPoint(44.6, 18.65)
            addMarkerToMap(point2, "Greenhouse 2")
            val point3 = GeoPoint(44.59, 18.69)
            addMarkerToMap(point3, "Greenhouse 3")

            greenhouseList = listOf(
                Greenhouse("Greenhouse 1", "58", "22", "6.2", R.drawable.greenhouse),
                Greenhouse("Greenhouse 2", "60", "23", "6.5", R.drawable.greenhouse),
                Greenhouse("Greenhouse 3", "65", "24", "6.1", R.drawable.greenhouse)
            )
            recyclerView = findViewById(R.id.recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = GreenhouseAdapter(greenhouseList)

            buttonDashboard.setOnClickListener {
                highlightButton(buttonDashboard)
            }

            buttonControl.setOnClickListener {
                val intent = Intent(this, ControlActivity::class.java)
                startActivity(intent)
                highlightButton(buttonControl)
            }

            buttonSettings.setOnClickListener {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                highlightButton(buttonSettings)
            }

        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun addMarkerToMap(point: GeoPoint, title: String) {
        val marker = Marker(mapView)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        mapView.overlays.add(marker)
    }

    private fun highlightButton(activeButton: Button) {
        buttonDashboard.setBackgroundColor(resources.getColor(android.R.color.transparent))
        buttonControl.setBackgroundColor(resources.getColor(android.R.color.transparent))
        buttonSettings.setBackgroundColor(resources.getColor(android.R.color.transparent))

        activeButton.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
}
