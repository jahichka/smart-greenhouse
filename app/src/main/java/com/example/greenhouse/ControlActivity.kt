package com.example.greenhouse

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ControlActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var greenhouseAdapter: ControlGreenhouseAdapter
    private lateinit var greenhouseList: MutableList<ControlGreenhouse>
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)

        sharedPreferences = getSharedPreferences("GreenhousePrefs", MODE_PRIVATE)

        greenhouseList = mutableListOf(
            ControlGreenhouse(
                "Greenhouse 1",
                "24",
                "60",
                "6.5",
                R.drawable.greenhouse,
                getToggleState("Greenhouse 1", "watering"),
                getToggleState("Greenhouse 1", "windows")
            ),
            ControlGreenhouse(
                "Greenhouse 2",
                "22",
                "55",
                "6.2",
                R.drawable.greenhouse,
                getToggleState("Greenhouse 2", "watering"),
                getToggleState("Greenhouse 2", "windows")
            ),
            ControlGreenhouse(
                "Greenhouse 3",
                "25",
                "65",
                "6.8",
                R.drawable.greenhouse,
                getToggleState("Greenhouse 3", "watering"),
                getToggleState("Greenhouse 3", "windows")
            )
        )

        recyclerView = findViewById(R.id.recycler_view_greenhouses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        greenhouseAdapter = ControlGreenhouseAdapter(this, greenhouseList) { greenhouse, toggleType, isChecked ->
            handleToggleChange(greenhouse, toggleType, isChecked)
        }
        recyclerView.adapter = greenhouseAdapter

        val addButton: ImageButton = findViewById(R.id.button_add_greenhouse)
        val editButton: ImageButton = findViewById(R.id.button_edit_greenhouse)
        val deleteButton: ImageButton = findViewById(R.id.button_delete_greenhouse)

        addButton.setOnClickListener { addGreenhouse() }
        editButton.setOnClickListener { editGreenhouse() }
        deleteButton.setOnClickListener { deleteGreenhouse() }

        setupBottomNavigation()
    }

    private fun handleToggleChange(greenhouse: ControlGreenhouse, toggleType: String, isChecked: Boolean) {
        when (toggleType) {
            "watering" -> {
                greenhouse.isWateringOn = isChecked
                saveToggleState(greenhouse.name, "watering", isChecked)
                Toast.makeText(this, "${greenhouse.name}: Watering ${if (isChecked) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
            }
            "windows" -> {
                greenhouse.isWindowsOpen = isChecked
                saveToggleState(greenhouse.name, "windows", isChecked)
                Toast.makeText(this, "${greenhouse.name}: Windows ${if (isChecked) "OPEN" else "CLOSED"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToggleState(greenhouseName: String, toggleType: String, isChecked: Boolean) {
        sharedPreferences.edit().putBoolean("$greenhouseName-$toggleType", isChecked).apply()
    }

    private fun getToggleState(greenhouseName: String, toggleType: String): Boolean {
        return sharedPreferences.getBoolean("$greenhouseName-$toggleType", false)
    }

    private fun addGreenhouse() {
        Toast.makeText(this, "Add Greenhouse functionality to be implemented.", Toast.LENGTH_SHORT).show()
    }

    private fun editGreenhouse() {
        Toast.makeText(this, "Edit Greenhouse functionality to be implemented.", Toast.LENGTH_SHORT).show()
    }

    private fun deleteGreenhouse() {
        Toast.makeText(this, "Delete Greenhouse functionality to be implemented.", Toast.LENGTH_SHORT).show()
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
}
