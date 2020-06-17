package com.example.flightmobileapp

import Api
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class Client {
    private val handler = Handler()
    var toastDuration = 4000
    var context: Context? = null
    constructor(context: Context) {
        this.context = context
    }
    fun sendControlsValues(aileron: Float, elevator:Float, rudder: Float, throttle: Float) {
        Toast.makeText(context, "Communication Error! please press back to return to main menu", Toast.LENGTH_LONG).show()
        val url = "http://10.0.2.2:59754/"
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val jsonObject = JsonObject()
        jsonObject.addProperty("aileron", aileron)
        jsonObject.addProperty("rudder", rudder)
        jsonObject.addProperty("elevator", elevator)
        jsonObject.addProperty("throttle", throttle)
        val service = retrofit.create(Api::class.java)
        val call = service.postData(jsonObject)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    Log.d("FlightMobileApp", response.body().toString())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Communication Error! please press back to return to main menu", toastDuration).show()
            }
        })
    }
    fun getImage(v: ImageView) {
        //add this task to the handler loop every 2 seconds to update the view
        //at the end of the task we re-add the task to the queue to work endlessly
        handler.postDelayed(object : Runnable{
            override fun run() {
                handler.postDelayed(this, 2000)
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
                        val bstream = response?.body()?.byteStream()
                        val bMap = BitmapFactory.decodeStream(bstream)
                        //runOnUiThread {
                        v.setImageBitmap(bMap)
                        //}
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        TODO("Not yet implemented")
                        Toast.makeText(context, "Communication Error! please press back to return to main menu", toastDuration).show()
                    }
                })
            }
        },2000)
    }
    fun stopClient() {
        handler.removeCallbacksAndMessages(null);
    }
}