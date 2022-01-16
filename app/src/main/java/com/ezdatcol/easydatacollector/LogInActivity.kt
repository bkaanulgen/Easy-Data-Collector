package com.ezdatcol.easydatacollector

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class LogInActivity : AppCompatActivity() {

    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        preferences = Preferences(this)

        val etLogin: EditText = findViewById(R.id.etLogin)
        val btLogin: Button = findViewById(R.id.btLogin)
        val tvLogin1: TextView = findViewById(R.id.tvLogin1)
        val tvLogin2: TextView = findViewById(R.id.tvLogin2)
        val tvLogin3: TextView = findViewById(R.id.tvLogin3)


        //  Set up TextView animation
        tvLogin1.alpha = 0f
        tvLogin2.alpha = 0f
        tvLogin3.alpha = 0f
        Handler().postDelayed({
            tvLogin1.animate().alpha(1f).duration = 1000
        }, 500)
        Handler().postDelayed({
            tvLogin2.animate().alpha(1f).duration = 1000
        }, 2000)
        Handler().postDelayed({
            tvLogin3.animate().alpha(1f).duration = 1000
        }, 3500)


        // Set up Button and EditText listeners
        btLogin.setOnClickListener {
            if (etLogin.text.toString() != "") {
                logIn(etLogin.text.toString().toUpperCase(Locale.ROOT))
            }
        }

        etLogin.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (etLogin.text.toString() != "" && keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                logIn(etLogin.text.toString().toUpperCase(Locale.ROOT))
                return@OnKeyListener true
            }
            false
        })
    }

    private fun logIn(plate: String) {
        preferences.savePlate(plate)
        preferences.saveIsLoggedIn(true)
        val intent = Intent(this, TutorialActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}