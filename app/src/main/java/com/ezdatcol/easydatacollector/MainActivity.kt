package com.ezdatcol.easydatacollector

import android.Manifest
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationRequest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import org.jetbrains.anko.doAsync
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    //  Do not modify
    private lateinit var serviceBroadcastReceiver: ServiceBroadcastReceiver
    private lateinit var locationUpdateService: LocationUpdateService
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationManager: LocationManager
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var database: FirebaseDatabase
    private lateinit var preferences: Preferences
    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0
    private var lastAlertedTime: Long = System.currentTimeMillis()
    private var qrWidth = 600
    private var qrHeight = 600
    private lateinit var qrImage: Bitmap
    private lateinit var qrToast: Toast
    private lateinit var btMenu: ImageButton
    private lateinit var btQR: Button
    private lateinit var btHelp: Button
    private lateinit var btLogout: Button
    private lateinit var btUpdate: Button
    private lateinit var btStartTrip: Button
    private lateinit var ratingMale: RatingBar
    private lateinit var ratingFemale: RatingBar
    private lateinit var btMaleRight: ImageView
    private lateinit var btMaleLeft: ImageView
    private lateinit var btFemaleRight: ImageView
    private lateinit var btFemaleLeft: ImageView
    private lateinit var tvMale: TextView
    private lateinit var tvFemale: TextView
    private lateinit var tvMain: TextView
    private var plateValue = ""
    private var passengerBooleanValue = 0
    private var passengerMaleValue = 0
    private var passengerFemaleValue = 0
    private var onTrip = false
    private var gpsInternetAlertShownOnce = false
    private var firstRun = true
    private var tripLengthAlertShownOnce = false
    private var tripLengthAlertShowingNow = false
    private var serviceBound = false
    private var permissionsGranted = false
    private var qrGenerated = false

    //  Can modify: app and trip settings
    private var qrToScreenRatio = 0.75       //  Upper limit of QR Code to screen ratio
    private val qrDimensions = 1000         //  Targeted QR Code dimensions
    private val coordinateDecimals = 5      //  Number of decimals of latitude and longitude
    private val maxPassengers = 4           //  Maximum number of passengers allowed
    private val maxTripLength = 10          //  Maximum trip length expected in minutes
    private val tripLengthAlertGap = 10     //  Time gap in minutes between two successive trip length alerts

    //  Can modify: database header and key values
    private val headerValue = "Taksi-10"
    private val passengerMaleName = "E"
    private val passengerFemaleName = "K"
    private val passengerBooleanName = "Y"
    private val latitudeName = "en"
    private val longitudeName = "boy"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  Get intent extras
        val bundle = intent.extras
        var origin = ""
        if (bundle != null) {
            origin = bundle.getString("origin").toString()
        }

        //  Set up permissions, get preferences, get database instance
        setupPermissions()
        locationRequest = LocationRequest()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        checkInternet()
        checkGps()
        database = Firebase.database

        //  Database write parameters
        preferences = Preferences(this)
        val savedSurveyLink = preferences.getSurveyLink().toString()
        plateValue = preferences.getSavedPlate().toString()

        //  Rating bars
        tvMain = findViewById(R.id.tvMain)
        tvMale = findViewById(R.id.tvMale)
        tvFemale = findViewById(R.id.tvFemale)
        ratingMale = findViewById(R.id.ratingMale)
        ratingFemale = findViewById(R.id.ratingFemale)
        btMaleRight = findViewById(R.id.btMaleRight)
        btMaleLeft = findViewById(R.id.btMaleLeft)
        btFemaleRight = findViewById(R.id.btFemaleRight)
        btFemaleLeft = findViewById(R.id.btFemaleLeft)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val rbUnselectedColor = ContextCompat.getColor(this, R.color.ratingBarUnselectedColor)
            val maleStars = ratingMale.progressDrawable as LayerDrawable
            maleStars.getDrawable(0).setColorFilter(rbUnselectedColor, PorterDuff.Mode.SRC_ATOP)
            val femaleStars = ratingFemale.progressDrawable as LayerDrawable
            femaleStars.getDrawable(0).setColorFilter(rbUnselectedColor, PorterDuff.Mode.SRC_ATOP)
        }

        ratingMale.rating = 1f
        ratingMale.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            tvMale.text = "ERKEK YOLCU: ${rating.toInt()}"
        }
        ratingFemale.rating = 0f
        ratingFemale.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            tvFemale.text = "KADIN YOLCU: ${rating.toInt()}"
        }
        btMaleRight.setOnClickListener {
            if (ratingMale.rating < 4f) ratingMale.rating++
        }
        btMaleLeft.setOnClickListener {
            if (ratingMale.rating > 0f) ratingMale.rating--
        }
        btFemaleRight.setOnClickListener {
            if (ratingFemale.rating < 4f) ratingFemale.rating++
        }
        btFemaleLeft.setOnClickListener {
            if (ratingFemale.rating > 0f) ratingFemale.rating--
        }

        //  Generate QR popup
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        qrWidth = minOf(qrDimensions, (minOf(displayMetrics.widthPixels.toDouble(), displayMetrics.heightPixels.toDouble()) * qrToScreenRatio).toInt())
        qrHeight = minOf(qrDimensions, (minOf(displayMetrics.widthPixels.toDouble(), displayMetrics.heightPixels.toDouble()) * qrToScreenRatio).toInt())
        if (savedSurveyLink != "") {
            doAsync {
                buildQR(savedSurveyLink)
            }
        } else {
            val ref = database.getReference("Survey").child("Link")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val link = dataSnapshot.getValue(String::class.java).toString()
                    doAsync {
                        buildQR(link)
                    }
                    preferences.saveSurveyLink(link)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        qrToast = Toast.makeText(this, getString(R.string.qr_button_toast_text), Toast.LENGTH_SHORT)
        btQR = findViewById(R.id.btQR)
        btQR.setOnClickListener { view ->
            if (qrGenerated) {
                qrToast.cancel()
                popupQR()
            } else {
                qrToast.show()
            }

        }

        //  Other buttons and views
        btStartTrip = findViewById(R.id.btStartTrip)
        btStartTrip.setOnClickListener { view ->
            if (!onTrip)
                prepareTrip(view)
            else
                requestEndTrip()
        }

        btLogout = findViewById(R.id.btLogout)
        btLogout.setOnClickListener {
            requestLogout()
        }

        btHelp = findViewById(R.id.btHelp)
        btHelp.setOnClickListener {
            val intent = Intent(this, TutorialActivity::class.java)
            intent.putExtra("fromMain", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        btMenu = findViewById(R.id.btMenu)
        var isMenuOpen = false
        changeMenuState(true)
        btMenu.setOnClickListener {
            changeMenuState(isMenuOpen)
            isMenuOpen = !isMenuOpen
        }

        //  Check if trip length exceeds expected max
        val tripLengthTimer = fixedRateTimer("tripLengthTimer", false, 0, 60 * 1000) {
            this@MainActivity.runOnUiThread {
                if (preferences.getIsTripStarted() && System.currentTimeMillis() >= preferences.getTripStartTime() + maxTripLength * 60 * 1000) {
                    val elapsedTime = ceil((System.currentTimeMillis() - preferences.getTripStartTime()).toDouble() / 60000).toInt()
                    if (!tripLengthAlertShownOnce) {
                        if (!tripLengthAlertShowingNow)
                            alertLongTrip(elapsedTime)
                        tripLengthAlertShowingNow = true
                        tripLengthAlertShownOnce = true
                        lastAlertedTime = System.currentTimeMillis()
                    } else if (System.currentTimeMillis() >= lastAlertedTime + tripLengthAlertGap * 60 * 1000) {
                        if (!tripLengthAlertShowingNow)
                            alertLongTrip(elapsedTime)
                        tripLengthAlertShowingNow = true
                        lastAlertedTime = System.currentTimeMillis()
                    }
                }
            }
        }

        //  Check for existing trip and ask for resume
        if (preferences.getIsTripStarted()) {
            if (origin == "notification_resume") {
                resumeTrip(false)
            } else {
                tripLengthAlertShownOnce = true
                alertInterruptedTrip(true)

            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus && !gpsInternetAlertShownOnce) {
            setupPermissions()
            checkInternet()
            checkGps()
        }
        super.onWindowFocusChanged(hasFocus)
    }

    override fun onResume() {
        super.onResume()

        if (!firstRun) {
            gpsInternetAlertShownOnce = false
            setupPermissions()
        } else {
            firstRun = false
        }

        // Start foreground service and broadcast listener
        if (!serviceBound && permissionsGranted && returnGpsState() && returnInternetState()) {
            locationUpdateService = LocationUpdateService()
            startService(Intent(this, LocationUpdateService::class.java))
            serviceBroadcastReceiver = ServiceBroadcastReceiver()
            val intentFilter = IntentFilter("LocationService")
            LocalBroadcastManager.getInstance(this).registerReceiver(serviceBroadcastReceiver, intentFilter)
            serviceBound = true
        }
    }

    private fun changeMenuState(isMenuOpen: Boolean) {
        val visibility: Int
        val menuResource: Int
        if (isMenuOpen) {
            visibility = View.INVISIBLE
            menuResource = R.drawable.ico_menu
        } else {
            visibility = View.VISIBLE
            menuResource = R.drawable.ico_menu_open
        }

        btMenu.setImageResource(menuResource)
        btQR.visibility = visibility
        btHelp.visibility = visibility
        btLogout.visibility = visibility

    }

    fun addToDatabase() {
        if (returnInternetState() && returnGpsState()) {
            val dateValue: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val timeValue: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val timeHeaderName = database.getReference(headerValue).child(plateValue).child(dateValue).child(timeValue)
            timeHeaderName.child(latitudeName).setValue(lastLatitude)
            timeHeaderName.child(longitudeName).setValue(lastLongitude)
            timeHeaderName.child(passengerBooleanName).setValue(passengerBooleanValue)
            timeHeaderName.child(passengerFemaleName).setValue(passengerFemaleValue)
            timeHeaderName.child(passengerMaleName).setValue(passengerMaleValue)
        }
    }

    private fun alertInterruptedTrip(showResumeText: Boolean) {
        val builder = AlertDialog.Builder(this)
        val elapsedTime = ceil((System.currentTimeMillis() - preferences.getTripStartTime()).toDouble() / 60000).toInt()
        val alertMessage = "$elapsedTime dakika önce başlattığınız yarım kalan yolculuğunuza devam etmek ister misiniz?"
        builder.setMessage(alertMessage)
            .setCancelable(false)
            .setNegativeButton("YOLCULUĞA DEVAM ET") { dialog, id ->
                resumeTrip(showResumeText)
                dialog.cancel()
            }
            .setPositiveButton("YOLCULUĞU İPTAL ET") { dialog, id ->
                endTrip(false)
                dialog.cancel()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.argb(255, 150, 0, 0))
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.argb(255, 0, 150, 0))

    }

    private fun alertLongTrip(tripLength: Int) {
        val builder = AlertDialog.Builder(this)
        val alertMessage = "$tripLength dakika önce başlattığınız yolculuğunuz hala devam ediyor. Yolculuğu sonlandırmak ister misiniz?"
        builder.setMessage(alertMessage)
            .setCancelable(true)
            .setNegativeButton("YOLCULUĞA DEVAM ET") { dialog, id ->
                Snackbar.make(findViewById(R.id.bgMainActivity), "Yolculuğa devam ediliyor.", Snackbar.LENGTH_SHORT).show()
                dialog.cancel()
            }
            .setPositiveButton("YOLCULUĞU SONLANDIR") { dialog, id ->
                endTrip(true)
                dialog.cancel()
            }
            .setOnCancelListener {
                tripLengthAlertShowingNow = false
            }
        val alert: AlertDialog = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.argb(255, 150, 0, 0))
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.argb(255, 0, 150, 0))
    }

    private fun prepareTrip(view: View) {
        if (ratingMale.rating == 0f && ratingFemale.rating == 0f) {
            Snackbar.make(view, "Lütfen en az 1 yolcu seçin.", Snackbar.LENGTH_LONG).show()
        } else {
//            (Optional) Check for max number of passengers
//            if (ratingMale.rating + ratingFemale.rating > maxPassengers.toFloat()) {
//                Snackbar.make(view, "Lütfen en fazla $maxPassengers yolcu seçin.", Snackbar.LENGTH_LONG).show()
//            } else {
            requestStartTrip()
//            }
        }
    }

    private fun startTrip() {
        onTrip = true
        preferences.saveTripStartTime(System.currentTimeMillis())
        preferences.saveIsTripStarted(true)
        passengerMaleValue = ratingMale.rating.toInt()
        passengerFemaleValue = ratingFemale.rating.toInt()
        preferences.saveMalePassengers(ratingMale.rating.toInt())
        preferences.saveFemalePassengers(ratingFemale.rating.toInt())
        passengerBooleanValue = 1
        btStartTrip.text = getString(R.string.main_button_negative_text)
        btStartTrip.setBackgroundResource(R.drawable.round_negative_button_background)
        btStartTrip.setTextColor(ResourcesCompat.getColor(resources, R.color.positiveButtonTextColor, null))
        switchTripView()
        btStartTrip.visibility = View.INVISIBLE
        tvMain.alpha = 0f
        tvMain.text = getString(R.string.main_trip_started_text)
        tvMain.animate().alpha(1f).duration = 500
        Handler().postDelayed({
            tvMain.animate().alpha(0f).duration = 500
        }, 1500)
        Handler().postDelayed({
            btStartTrip.visibility = View.VISIBLE
        }, 2000)
    }

    private fun endTrip(showEndText: Boolean) {
        onTrip = false
        tripLengthAlertShownOnce = false
        preferences.saveIsTripStarted(false)
        preferences.saveTripStartTime(0)
        preferences.saveMalePassengers(0)
        preferences.saveFemalePassengers(0)
        ratingMale.rating = 1f
        ratingFemale.rating = 0f
        passengerMaleValue = 0
        passengerFemaleValue = 0
        passengerBooleanValue = 0
        btStartTrip.text = getString(R.string.main_button_positive_text)
        btStartTrip.setBackgroundResource(R.drawable.round_positive_button_background)
        btStartTrip.setTextColor(ResourcesCompat.getColor(resources, R.color.positiveButtonTextColor, null))
        tvMain.alpha = 0f
        if (showEndText) {
            btStartTrip.visibility = View.INVISIBLE
            tvMain.text = getString(R.string.main_trip_ended_text)
            tvMain.animate().alpha(1f).duration = 500
            Handler().postDelayed({
                tvMain.animate().alpha(0f).duration = 500
            }, 1500)
            Handler().postDelayed({
                btStartTrip.visibility = View.VISIBLE
                switchTripView()
            }, 2000)
        }
    }

    private fun resumeTrip(showResumeText: Boolean) {
        onTrip = true
        passengerMaleValue = preferences.getMalePassengers()
        passengerFemaleValue = preferences.getFemalePassengers()
        passengerBooleanValue = 1
        btStartTrip.text = getString(R.string.main_button_negative_text)
        btStartTrip.setBackgroundResource(R.drawable.round_negative_button_background)
        btStartTrip.setTextColor(ResourcesCompat.getColor(resources, R.color.negativeButtonTextColor, null))
        switchTripView()
        tvMain.alpha = 0f
        if (showResumeText) {
            btStartTrip.visibility = View.INVISIBLE
            tvMain.text = getString(R.string.main_trip_resumed_text)
            tvMain.animate().alpha(1f).duration = 500
            Handler().postDelayed({
                tvMain.animate().alpha(0f).duration = 500
            }, 1500)
            Handler().postDelayed({
                btStartTrip.visibility = View.VISIBLE
            }, 2000)
        }
    }

    private fun switchTripView() {
        val visibility = if (onTrip) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }

        ratingMale.visibility = visibility
        ratingFemale.visibility = visibility
        btMaleRight.visibility = visibility
        btMaleLeft.visibility = visibility
        btFemaleRight.visibility = visibility
        btFemaleLeft.visibility = visibility
        tvFemale.visibility = visibility
        tvMale.visibility = visibility
    }

    private fun requestStartTrip() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("${ratingMale.rating.toInt()} erkek ve ${ratingFemale.rating.toInt()} kadın yolcu seçtiniz. Yolculuğu başlatmak istediğinizden emin misiniz?")
            .setCancelable(true)
            .setPositiveButton("YOLCULUĞU BAŞLAT") { dialog, id ->
                dialog.cancel()
                startTrip()
            }
            .setNegativeButton("GERİ DÖN") { dialog, id ->
                dialog.cancel()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.argb(255, 150, 0, 0))
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.argb(255, 0, 150, 0))
    }

    private fun requestEndTrip() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Yolculuğu sonlandırmak istediğinizden emin misiniz?")
            .setCancelable(true)
            .setPositiveButton("YOLCULUĞU SONLANDIR") { dialog, id ->
                dialog.cancel()
                endTrip(true)
            }
            .setNegativeButton("GERİ DÖN") { dialog, id ->
                dialog.cancel()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.argb(255, 150, 0, 0))
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.argb(255, 0, 150, 0))
    }

    private fun requestLogout() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Uygulamadan çıkış yapmak istediğinizden emin misiniz?")
            .setCancelable(true)
            .setPositiveButton("UYGULAMADAN ÇIKIŞ YAP") { dialog, id ->
                LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceBroadcastReceiver)
                stopService(Intent(this, LocationUpdateService::class.java))
                val intent = Intent(this, LogOutActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(0, 0)
            }
            .setNegativeButton("UYGULAMAYA GERİ DÖN") { dialog, id ->
                dialog.cancel()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.argb(255, 150, 0, 0))
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.argb(255, 0, 150, 0))
    }

    private fun setupPermissions() {
        var perm = true
        val internetPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
        if (internetPermission != PackageManager.PERMISSION_GRANTED) {
            perm = false
            makeRequest(Manifest.permission.INTERNET)
        }
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            perm = false
            makeRequest(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            perm = false
            makeRequest(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val backgroundLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            if (backgroundLocationPermission != PackageManager.PERMISSION_GRANTED) {
                perm = false
                makeRequest(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val foregroundService = ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
            if (foregroundService != PackageManager.PERMISSION_GRANTED) {
                perm = false
                makeRequest(Manifest.permission.FOREGROUND_SERVICE)
            }
        }
        permissionsGranted = perm
    }

    private fun makeRequest(permissionName: String) {
        ActivityCompat.requestPermissions(this, arrayOf(permissionName), 0)
    }

    private fun returnGpsState(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkGps() {
        if (!returnGpsState() && !gpsInternetAlertShownOnce) {
            gpsInternetAlertShownOnce = true
            alertNoGps()
        }
    }

    private fun alertNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Uygulamayı kullanabilmek için lütfen konum hizmetlerini etkinleştirin")
            .setCancelable(false)
            .setPositiveButton("UYGULAMADAN ÇIKIŞ YAP") { dialog, id ->
                dialog.cancel()
                finish()
            }
            .setNegativeButton("KONUM HİZMETLERİNİ ETKİNLEŞTİR") { dialog, id ->
                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 11)

            }
        val alert: AlertDialog = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.argb(255, 150, 0, 0))
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.argb(255, 0, 150, 0))
    }

    private fun returnInternetState(): Boolean {
        var result = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.run {
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }

    private fun checkInternet() {
        if (!returnInternetState() && !gpsInternetAlertShownOnce) {
            gpsInternetAlertShownOnce = true
            alertNoInternet()
        }
    }

    private fun alertNoInternet() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Uygulamayı kullanabilmek için lütfen mobil veriyi etkinleştirin")
            .setCancelable(false)
            .setPositiveButton("UYGULAMADAN ÇIKIŞ YAP") { dialog, id ->
                dialog.cancel()
                finish()
            }
            .setNegativeButton("MOBİL VERİYİ ETKİNLEŞTİR") { dialog, id ->
                startActivityForResult(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS), 11)

            }
        val alert: AlertDialog = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.argb(255, 150, 0, 0))
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.argb(255, 0, 150, 0))
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun buildQR(link: String) {
        qrImage = Bitmap.createBitmap(qrWidth, qrHeight, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(link, BarcodeFormat.QR_CODE, qrWidth, qrHeight)
            for (x in 0 until qrWidth) {
                for (y in 0 until qrHeight) {
                    qrImage.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            qrGenerated = true
            Toast.makeText(this, "generated", Toast.LENGTH_SHORT).show()
        } catch (e: WriterException) {
        }
    }

    private fun popupQR() {
        val builder = Dialog(this)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val imageView = ImageView(this)
        imageView.setImageBitmap(qrImage)
        builder.addContentView(imageView, RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        builder.show()
    }

    inner class ServiceBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            if (bundle != null) {
                lastLatitude = (bundle.getDouble("latitude") * 10.0.pow(coordinateDecimals)).roundToInt() / 10.0.pow(coordinateDecimals)
                lastLongitude = (bundle.getDouble("longitude") * 10.0.pow(coordinateDecimals)).roundToInt() / 10.0.pow(coordinateDecimals)
                addToDatabase()
            }
        }
    }
}

