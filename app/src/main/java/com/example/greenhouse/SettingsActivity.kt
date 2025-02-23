package com.example.greenhouse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)

        auth = FirebaseAuth.getInstance()
        val themeSwitch: Switch = findViewById(R.id.switch_theme_preference)

        val languageManager = LanguageManager(this)
        val buttonLanguage: Button = findViewById(R.id.button_language)

        buttonLanguage.setOnClickListener {
            val newLanguage = if (languageManager.getLanguage() == "en") "bs" else "en"
            languageManager.setLocale(newLanguage)
            recreate()
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Dark theme enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Light theme enabled", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<TextView>(R.id.text_privacy_and_security).setOnClickListener {
            Toast.makeText(this, "Privacy and Security clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.button_dashboard).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_control).setOnClickListener {
            val intent = Intent(this, ControlActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_settings).setOnClickListener {
            Toast.makeText(this, "You are already on Settings", Toast.LENGTH_SHORT).show()
        }
        val logoutButton: Button = findViewById(R.id.button_logout)
        logoutButton.setOnClickListener {
            auth.signOut()
            navigateToLogin()
        }
    }
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clears activity stack
        startActivity(intent)
        finish()
    }
}
