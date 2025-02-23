package com.example.greenhouse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class GreenhouseFormActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var nameEditText: EditText
    private lateinit var locationTextView: TextView
    private lateinit var pickLocationButton: Button
    private lateinit var saveButton: Button

    private var greenhouseId: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_greenhouse_form)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nameEditText = findViewById(R.id.edit_text_name)
        locationTextView = findViewById(R.id.text_view_location)
        pickLocationButton = findViewById(R.id.button_pick_location)
        saveButton = findViewById(R.id.button_save)

        greenhouseId = intent.getStringExtra("greenhouseId")

        if (greenhouseId != null) {
            loadGreenhouseData()
        }

        pickLocationButton.setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            startActivityForResult(intent, REQUEST_LOCATION_PICKER)
        }

        saveButton.setOnClickListener {
            saveGreenhouse()
        }
    }

    private fun loadGreenhouseData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("greenhouses").document(greenhouseId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    nameEditText.setText(document.getString("name"))

                    val geoPoint = document.getGeoPoint("location")
                    if (geoPoint != null) {
                        latitude = geoPoint.latitude
                        longitude = geoPoint.longitude
                        locationTextView.text = "Lat: $latitude, Lng: $longitude"
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load greenhouse data.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveGreenhouse() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            return
        }

        if (latitude == null || longitude == null) {
            Toast.makeText(this, "Please select a location.", Toast.LENGTH_SHORT).show()
            return
        }

        val greenhouseData = mapOf(
            "name" to nameEditText.text.toString(),
            "location" to GeoPoint(latitude!!, longitude!!)
        )

        val userCollection = db.collection("users").document(userId).collection("greenhouses")

        if (greenhouseId == null) {
            userCollection.add(greenhouseData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Greenhouse added!", Toast.LENGTH_SHORT).show()
                    finish() // Close activity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add greenhouse.", Toast.LENGTH_SHORT).show()
                }
        } else {
            userCollection.document(greenhouseId!!)
                .update(greenhouseData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Greenhouse updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update greenhouse.", Toast.LENGTH_SHORT).show()
                }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOCATION_PICKER && resultCode == RESULT_OK) {
            latitude = data?.getDoubleExtra("latitude", 0.0)
            longitude = data?.getDoubleExtra("longitude", 0.0)

            if (latitude != null && longitude != null) {
                locationTextView.text = "Lat: $latitude, Lng: $longitude"
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PICKER = 1001
    }
}
