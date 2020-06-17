package com.example.flightmobileapp
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun connectButtonOnClick(view: View) {
        openFlightAppActivity();
        }
    fun openFlightAppActivity() {
        val intent = Intent(this, FlightAppActivity::class.java)
        startActivity(intent)
        //val intent = Intent(this, JoystickActivity::class.java)
        //startActivity(intent)
    }
}