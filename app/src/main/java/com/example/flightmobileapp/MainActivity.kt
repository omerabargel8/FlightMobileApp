package com.example.flightmobileapp
import Api
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_flight_app.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun connectButtonOnClick(view: View) {
            val url = urlTextEdit.text.toString()
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:59754/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            val api = retrofit.create(Api::class.java)
            val body = api.getImg().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        openFlightAppActivity()
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@MainActivity,"Communication Failure! please try again later or use different URL", Toast.LENGTH_SHORT).show()
                }
            })
    }
    fun openFlightAppActivity() {
        val intent = Intent(this, FlightAppActivity::class.java)
        startActivity(intent)
        //val intent = Intent(this, JoystickActivity::class.java)
        //startActivity(intent)
    }
}