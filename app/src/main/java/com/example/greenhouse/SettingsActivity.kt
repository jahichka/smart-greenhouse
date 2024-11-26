package com.example.greenhouse

import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)

        val themeSwitch: Switch = findViewById(R.id.switch_theme_preference)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Dark theme enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Light theme enabled", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<TextView>(R.id.text_account_preferences).setOnClickListener {
            Toast.makeText(this, "Account Preferences clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.text_privacy_and_security).setOnClickListener {
            Toast.makeText(this, "Privacy and Security clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.button_dashboard).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.button_control).setOnClickListener {
            val intent = Intent(this, ControlActivity::class.java)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.button_settings).setOnClickListener {
            Toast.makeText(this, "You are already on Settings", Toast.LENGTH_SHORT).show()
        }
    }
}
