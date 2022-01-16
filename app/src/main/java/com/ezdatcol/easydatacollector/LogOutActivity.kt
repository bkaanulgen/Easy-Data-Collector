package com.ezdatcol.easydatacollector

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LogOutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        val preferences = Preferences(this)
        preferences.savePlate("")
        preferences.saveIsLoggedIn(false)
        preferences.saveIsTripStarted(false)
        preferences.saveTripStartTime(0)
        preferences.saveMalePassengers(0)
        preferences.saveFemalePassengers(0)

        //  Animate main text
        val tvLogout: TextView = findViewById(R.id.tvLogout)
        tvLogout.alpha = 0f
        tvLogout.animate().alpha(1f).duration = 500
        Handler().postDelayed({
            tvLogout.animate().alpha(0f).duration = 500
        }, 1500)


        //  Redirect to splash screen
        Handler().postDelayed({
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }, 2000)
    }

    override fun onBackPressed() {
    }
}