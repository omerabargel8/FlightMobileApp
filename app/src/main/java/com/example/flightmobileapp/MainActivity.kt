package com.example.flightmobileapp
import Api
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var db: HostDatabase? = null
    private var hostViewModel: HostViewModel? = null
    private var hostDao: HostDao? = null
    private var buttonManager : ArrayList<Button> = ArrayList()

    private fun init() {
        db = HostDatabase.getDB(this)
        hostDao = db?.hostDao()
        hostViewModel = ViewModelProvider(this).get(HostViewModel::class.java)
        updateButtonManager()
        initUrlAddresses()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }
    fun connectButtonOnClick(view: View) {
        val url:String = urlTextEdit.text.toString()
        updateUrl(url)
        try {

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
                    openFlightAppActivity()
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@MainActivity,"Communication Failure! please try again later or use different URL", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e:Exception) {
            Toast.makeText(this@MainActivity,"Communication Failure! please try again later or use different URL", Toast.LENGTH_SHORT).show()
        }
    }
    fun openFlightAppActivity() {
        val intent = Intent(this, FlightAppActivity::class.java)
        startActivity(intent)
        //val intent = Intent(this, JoystickActivity::class.java)
        //startActivity(intent)
    }
    fun updateButtonManager() {
        buttonManager.add(url1)
        buttonManager.add(url2)
        buttonManager.add(url3)
        buttonManager.add(url4)
        buttonManager.add(url5)
    }
    fun initUrlAddresses() {
        var size:Int = 0;
        val hostList : List<Host>? =  hostViewModel?.getAllHosts(hostDao)
        if (hostList != null) {
            size = hostList.size
            if(size > 5)
                size = 5;
            for (i in 0..(size-1)) {
                buttonManager[i].text = hostList[i].url
            }
        }
    }
    fun updateUrl(url: String) {
        hostViewModel?.insert(Host(url), hostDao)
        initUrlAddresses()
    }
    fun urlOnClick(v:View) {
        when (v.id) {
            url1.id -> urlTextEdit.setText(url1.text)
            url2.id -> urlTextEdit.setText(url2.text)
            url3.id -> urlTextEdit.setText(url3.text)
            url4.id -> urlTextEdit.setText(url4.text)
            url5.id -> urlTextEdit.setText(url5.text)
        }
    }

}