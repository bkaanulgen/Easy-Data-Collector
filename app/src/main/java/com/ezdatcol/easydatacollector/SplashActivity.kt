package com.ezdatcol.easydatacollector

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Get log in data
        val preferences = Preferences(this)
        val savedLoggedIn = preferences.getIsLoggedIn()

        // Retrieve survey link from database
        val database = Firebase.database
        val ref = database.getReference("Survey").child("Link")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val linkValue = dataSnapshot.getValue(String::class.java).toString()
                if (linkValue != "") {
                    preferences.saveSurveyLink(linkValue)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        // Splash logo animation
        val imgSplashLogo: ImageView = findViewById(R.id.imgSplashLogo)
        imgSplashLogo.alpha = 0f
        imgSplashLogo.animate().alpha(1f).duration = 500
        Handler().postDelayed({
            imgSplashLogo.animate().alpha(0f).duration = 500
        }, 1000)

        // Proceed to the next activity
        Handler().postDelayed({
            var intent = Intent()
            if (savedLoggedIn) {
                intent = Intent(this, MainActivity::class.java)
                intent.putExtra("origin", "splash")
            } else {
                intent = Intent(this, LogInActivity::class.java)
            }
            startActivity(intent)
            finish()
            overridePendingTransition(0, 0)
        }, 1500)
    }

    override fun onBackPressed() {
    }
}