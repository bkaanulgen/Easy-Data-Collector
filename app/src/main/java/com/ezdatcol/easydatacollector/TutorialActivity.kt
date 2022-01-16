package com.ezdatcol.easydatacollector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import me.relex.circleindicator.CircleIndicator3

class TutorialActivity : AppCompatActivity() {

    private var imageList = mutableListOf<Int>()
    private var textList = mutableListOf<String>()
    private var fromMain = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        //  Get origin
        val bundle = intent.extras
        if (bundle != null) {
            fromMain = bundle.getBoolean("fromMain")
        }

        //  Set up slider
        composeList()
        val vpTutorial: ViewPager2 = findViewById(R.id.vpTutorial)
        vpTutorial.adapter = ViewPagerAdapter(imageList, textList)
        vpTutorial.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        //  Set up indicator
        val indTutorial: CircleIndicator3 = findViewById(R.id.indTutorial)
        indTutorial.setViewPager(vpTutorial)

        // Set up button
        val btNextTutorial: Button = findViewById(R.id.btNextTutorial)
        btNextTutorial.setOnClickListener {
            if (vpTutorial.currentItem == imageList.size - 1) {
                if (fromMain) {
                    onBackPressed()
                } else {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("origin", "tutorial")
                    startActivity(intent)
                    finish()
                    overridePendingTransition(0, 0)
                }
            } else {
                vpTutorial.currentItem += 1
            }
        }
    }

    private fun composeList() {
        addToList(R.drawable.img_empty, getString(R.string.tutorial_main_0))
        addToList(R.drawable.gif_tutorial_1, getString(R.string.tutorial_main_1))
        addToList(R.drawable.gif_tutorial_2, getString(R.string.tutorial_main_2))
        addToList(R.drawable.gif_tutorial_3, getString(R.string.tutorial_main_3))
        addToList(R.drawable.gif_tutorial_4, getString(R.string.tutorial_main_4))
        addToList(R.drawable.gif_tutorial_5, getString(R.string.tutorial_main_5))
        addToList(R.drawable.gif_tutorial_6, getString(R.string.tutorial_main_6))
    }

    private fun addToList(image: Int, text: String) {
        imageList.add(image)
        textList.add(text)
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        if (fromMain) {
            super.onBackPressed()
            return
        } else {
            moveTaskToBack(true)
        }
    }
}