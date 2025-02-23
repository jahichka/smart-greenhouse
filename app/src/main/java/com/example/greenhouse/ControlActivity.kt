package com.example.greenhouse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ControlActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var greenhouseAdapter: ControlGreenhouseAdapter
    private lateinit var greenhouseList: MutableList<ControlGreenhouse>
    private lateinit var db: FirebaseFirestore
    private var snapshotListener: ListenerRegistration? = null
    private lateinit var auth: FirebaseAuth

    private var isEditMode = false
    private var isDeleteMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        greenhouseList = mutableListOf()

        recyclerView = findViewById(R.id.recycler_view_greenhouses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        greenhouseAdapter = ControlGreenhouseAdapter(this, greenhouseList, ::handleToggleChange, ::handleGreenhouseClick)
        recyclerView.adapter = greenhouseAdapter

        val addButton: ImageButton = findViewById(R.id.button_add_greenhouse)
        val editButton: ImageButton = findViewById(R.id.button_edit_greenhouse)
        val deleteButton: ImageButton = findViewById(R.id.button_delete_greenhouse)

        addButton.setOnClickListener {
            startActivity(Intent(this, GreenhouseFormActivity::class.java))
        }

        editButton.setOnClickListener {
            isEditMode = true
            isDeleteMode = false
            Toast.makeText(this, "Select a greenhouse to edit", Toast.LENGTH_LONG).show()
        }

        deleteButton.setOnClickListener {
            isDeleteMode = true
            isEditMode = false
            Toast.makeText(this, "Tap a greenhouse to delete", Toast.LENGTH_LONG).show()
        }

        setupBottomNavigation()
        setupFirebaseListener()
    }

    private fun handleGreenhouseClick(greenhouse: ControlGreenhouse) {
        when {
            isEditMode -> {
                isEditMode = false
                val intent = Intent(this, GreenhouseFormActivity::class.java)
                intent.putExtra("greenhouseId", greenhouse.documentId)
                startActivity(intent)
            }

            isDeleteMode -> {
                confirmDelete(greenhouse)
            }
        }
    }

    private fun confirmDelete(greenhouse: ControlGreenhouse) {
        AlertDialog.Builder(this)
            .setTitle("Delete Greenhouse")
            .setMessage("Are you sure you want to delete '${greenhouse.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteGreenhouse(greenhouse)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteGreenhouse(greenhouse: ControlGreenhouse) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("greenhouses").document(greenhouse.documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Greenhouse deleted!", Toast.LENGTH_SHORT).show()
                greenhouseList.remove(greenhouse)
                greenhouseAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete greenhouse.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupFirebaseListener() {
        val userId = auth.currentUser?.uid ?: return

        snapshotListener = db.collection("users").document(userId)
            .collection("greenhouses")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading greenhouses: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    greenhouseList.clear()
                    for (document in snapshot.documents) {
                        document?.let {
                            Log.d("FirestoreData", "Temperature: ${it.getString("temperature")}, Humidity: ${it.getString("humidity")}, Acidity: ${it.getString("acidity")}")
                        }

                        val greenhouse = document.toObject(ControlGreenhouse::class.java)?.apply {
                            documentId = document.id
                            isWateringOn = document.getBoolean("isWateringOn") ?: false  // ✅ Default to false if missing
                            isWindowsOpen = document.getBoolean("isWindowsOpen") ?: false  // ✅ Default to false if missing
                            temperature = document.getString("temperature") ?: "N/A"  // ✅ Now updating temperature
                            humidity = document.getString("humidity") ?: "N/A"      // ✅ Now updating humidity
                            acidity = document.getString("acidity") ?: "N/A"

                        }
                        if (greenhouse != null) {
                            greenhouseList.add(greenhouse)
                        }
                    }
                    greenhouseAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun handleToggleChange(greenhouse: ControlGreenhouse, toggleType: String, isChecked: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val documentRef = db.collection("users").document(userId)
            .collection("greenhouses").document(greenhouse.documentId)

        val updateField = when (toggleType) {
            "watering" -> "isWateringOn"
            "windows" -> "isWindowsOpen"
            else -> return
        }

        documentRef.update(updateField, isChecked)
            .addOnSuccessListener {
                if (toggleType == "watering") {
                    greenhouse.isWateringOn = isChecked
                } else {
                    greenhouse.isWindowsOpen = isChecked
                }
                greenhouseAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupBottomNavigation() {
        val dashboardButton: Button = findViewById(R.id.button_dashboard)
        val controlButton: Button = findViewById(R.id.button_control)
        val settingsButton: Button = findViewById(R.id.button_settings)

        dashboardButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        controlButton.setOnClickListener {
            Toast.makeText(this, "You're already on the Control tab.", Toast.LENGTH_SHORT).show()
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }
}

