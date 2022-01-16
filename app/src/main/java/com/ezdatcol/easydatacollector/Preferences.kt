package com.ezdatcol.easydatacollector

import android.content.Context

class Preferences(context: Context) {

    val PREFERENCE_PLATE = "Plate"
    val PREFERENCE_SAVED_PLATE = "PlateValue"
    val PREFERENCE_IS_LOGGED_IN = "IsLoggedIn"
    val PREFERENCE_SAVED_IS_LOGGED_IN = "IsLoggedInValue"
    val PREFERENCE_SURVEY_LINK = "SurveyLink"
    val PREFERENCE_SAVED_SURVEY_LINK = "SurveyLinkValue"
    val PREFERENCE_IS_TRIP_STARTED = "IsTripStarted"
    val PREFERENCE_SAVED_IS_TRIP_STARTED = "IsTripStartedValue"
    val PREFERENCE_MALE_PASSENGERS = "MalePassengers"
    val PREFERENCE_SAVED_MALE_PASSENGERS = "MalePassengersValue"
    val PREFERENCE_FEMALE_PASSENGERS = "FemalePassengers"
    val PREFERENCE_SAVED_FEMALE_PASSENGERS = "FemalePassengersValue"
    val PREFERENCE_TRIP_START_TIME = "TripStartTime"
    val PREFERENCE_SAVED_TRIP_START_TIME = "TripStartTimeValue"

    val plate = context.getSharedPreferences(PREFERENCE_PLATE, Context.MODE_PRIVATE)
    val isLoggedIn = context.getSharedPreferences(PREFERENCE_IS_LOGGED_IN, Context.MODE_PRIVATE)
    val surveyLink = context.getSharedPreferences(PREFERENCE_SURVEY_LINK, Context.MODE_PRIVATE)
    val isTripStarted = context.getSharedPreferences(PREFERENCE_IS_TRIP_STARTED, Context.MODE_PRIVATE)
    val malePassengers = context.getSharedPreferences(PREFERENCE_MALE_PASSENGERS, Context.MODE_PRIVATE)
    val femalePassengers = context.getSharedPreferences(PREFERENCE_FEMALE_PASSENGERS, Context.MODE_PRIVATE)
    val tripStartTime = context.getSharedPreferences(PREFERENCE_TRIP_START_TIME, Context.MODE_PRIVATE)

    fun getSavedPlate(): String? {
        return plate.getString(PREFERENCE_SAVED_PLATE, "")
    }

    fun savePlate(value: String) {
        val editor = plate.edit()
        editor.putString(PREFERENCE_SAVED_PLATE, value)
        editor.apply()
    }

    fun getIsLoggedIn(): Boolean {
        return isLoggedIn.getBoolean(PREFERENCE_SAVED_IS_LOGGED_IN, false)
    }

    fun saveIsLoggedIn(value: Boolean) {
        val editor = isLoggedIn.edit()
        editor.putBoolean(PREFERENCE_SAVED_IS_LOGGED_IN, value)
        editor.apply()
    }

    fun getSurveyLink(): String? {
        return surveyLink.getString(PREFERENCE_SAVED_SURVEY_LINK, "")
    }

    fun saveSurveyLink(value: String) {
        val editor = surveyLink.edit()
        editor.putString(PREFERENCE_SAVED_SURVEY_LINK, value)
        editor.apply()
    }

    fun getIsTripStarted(): Boolean {
        return isTripStarted.getBoolean(PREFERENCE_SAVED_IS_TRIP_STARTED, false)
    }

    fun saveIsTripStarted(value: Boolean) {
        val editor = isTripStarted.edit()
        editor.putBoolean(PREFERENCE_SAVED_IS_TRIP_STARTED, value)
        editor.apply()
    }

    fun getMalePassengers(): Int {
        return malePassengers.getInt(PREFERENCE_SAVED_MALE_PASSENGERS, 0)
    }

    fun saveMalePassengers(value: Int) {
        val editor = malePassengers.edit()
        editor.putInt(PREFERENCE_SAVED_MALE_PASSENGERS, value)
        editor.apply()
    }

    fun getFemalePassengers(): Int {
        return femalePassengers.getInt(PREFERENCE_SAVED_FEMALE_PASSENGERS, 0)
    }

    fun saveFemalePassengers(value: Int) {
        val editor = femalePassengers.edit()
        editor.putInt(PREFERENCE_SAVED_FEMALE_PASSENGERS, value)
        editor.apply()
    }

    fun getTripStartTime(): Long {
        return tripStartTime.getLong(PREFERENCE_SAVED_TRIP_START_TIME, 0)
    }

    fun saveTripStartTime(value: Long) {
        val editor = tripStartTime.edit()
        editor.putLong(PREFERENCE_SAVED_TRIP_START_TIME, value)
        editor.apply()
    }
}