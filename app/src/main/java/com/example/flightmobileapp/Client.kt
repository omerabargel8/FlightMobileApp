package com.example.flightmobileapp

import Api
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
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


class Client {
    private val handler = Handler()
    var toastDuration = 4000
    var context: Context? = null
    constructor(context: Context) {
        this.context = context
    }
    fun sendControlsValues(url :String, aileron: Float, elevator:Float, rudder: Float, throttle: Float) {
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

                if (response.code() != 200)
                    Toast.makeText(context, "Communication Error! please press back to return to main menu", toastDuration).show()
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Communication Error! please press back to return to main menu", toastDuration).show()
            }
        })
    }
    fun getImage(v: ImageView, url:String) {
        //add this task to the handler loop every 2 seconds to update the view
        //at the end of the task we re-add the task to the queue to work endlessly
        handler.postDelayed(object : Runnable{
            override fun run() {
                handler.postDelayed(this, 2000)
                val gson = GsonBuilder()
                    .setLenient()
                    .create()
                val retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                val api = retrofit.create(Api::class.java)
                val body = api.getImg().enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.code() == 200) {
                            val bstream = response?.body()?.byteStream()
                            val bMap = BitmapFactory.decodeStream(bstream)
                            v.setImageBitmap(bMap)
                        } else
                            Toast.makeText(context, "Communication Error! please press back to return to main menu", toastDuration).show()
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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