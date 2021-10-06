package com.example.weatherapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.net.URI
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {


    lateinit var tvName : TextView
    lateinit var tvTime : TextView
    lateinit var tvDescription : TextView
    lateinit var tvTemp : TextView
    lateinit var tvLow : TextView
    lateinit var tvHigh : TextView
    lateinit var tvSunrise : TextView
    lateinit var tvSunset : TextView
    lateinit var tvWind : TextView
    lateinit var tvHumidity : TextView
    lateinit var tvRefresh : TextView
    lateinit var tvPressure : TextView
    lateinit var vFirst : LinearLayout
    lateinit var llFirst : LinearLayout
    lateinit var llZipCity : LinearLayout
    lateinit var llIcons : LinearLayout
    lateinit var llText : LinearLayout
    lateinit var llIcons2 : LinearLayout
    lateinit var llText2 : LinearLayout
    lateinit var bSubmit : Button
    lateinit var eCity : EditText
    lateinit var bError : Button
    lateinit var llError : LinearLayout





    private val API = "ddc808395f8920bb8c14e77ed278b82c"
    private var city = "10001"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        tvName = findViewById(R.id.tvName)
        tvTime = findViewById(R.id.tvTime)
        tvDescription = findViewById(R.id.tvDescription)
        tvTemp = findViewById(R.id.tvTemp)
        tvLow = findViewById(R.id.tvLow)
        tvHigh = findViewById(R.id.tvHigh)
        tvSunrise = findViewById(R.id.tvSunrise)
        tvSunset = findViewById(R.id.tvSunset)
        tvWind = findViewById(R.id.tvWind)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvRefresh = findViewById(R.id.tvRefresh)
        tvPressure = findViewById(R.id.tvPressure)
        vFirst = findViewById(R.id.vFirst)
        llFirst = findViewById(R.id.llFirst)
        llZipCity = findViewById(R.id.llZipCity)
        llIcons = findViewById(R.id.llIcons)
        llText = findViewById(R.id.llText)
        llIcons2 = findViewById(R.id.llIcons2)
        llText2 = findViewById(R.id.llText2)
        bSubmit = findViewById(R.id.bSubmitCity)
        eCity = findViewById(R.id.eCity)
        bError = findViewById(R.id.bError)
        llError = findViewById(R.id.llErrorContainer)


        llZipCity.visibility = View.INVISIBLE

        bError.setOnClickListener {
            city = "10001"
            requestAPI()
        }

        requestAPI()

        tvName.setOnClickListener{
         change(0)
        }

        bSubmit.setOnClickListener {
            city = eCity.text.toString()
            requestAPI()
            change(1)
            // Hide Keyboard
            val view:View? = this.currentFocus
            if(view != null) {
                val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
                imm?.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
            }

        }
        requestAPI()
    }

    fun change(mode : Int){
        if (mode==0){
            vFirst.visibility = View.INVISIBLE
            llFirst.visibility = View.INVISIBLE
            llIcons.visibility = View.INVISIBLE
            llText.visibility = View.INVISIBLE
            llIcons2.visibility = View.INVISIBLE
            llText2.visibility = View.INVISIBLE
            llZipCity.visibility = View.VISIBLE

        }

        else{
            vFirst.visibility = View.VISIBLE
            llFirst.visibility = View.VISIBLE
            llIcons.visibility = View.VISIBLE
            llText.visibility = View.VISIBLE
            llIcons2.visibility = View.VISIBLE
            llText2.visibility = View.VISIBLE
            llZipCity.visibility = View.INVISIBLE
        }
    }

    private fun requestAPI(){


        CoroutineScope(IO).launch {
            val data = async { fetchData() }.await()

            if (data.isNotEmpty()){
                populateData(data)
            }
        }
    }

    private fun fetchData() : String{

        var response = ""
        try{
            response = URL("https://api.openweathermap.org/data/2.5/weather?zip=$city&units=metric&appid=$API").readText()
        }catch (e: Exception){
            Log.d("Main", "Issue: $e")
        }
        return response
    }

    private suspend fun populateData(result : String){
        withContext(Main){
            val jsonObject = JSONObject(result)

            //Declaration from API
            val sys = jsonObject.getJSONObject("sys")
            val weather = jsonObject.getJSONArray("weather").getJSONObject(0)
            val main = jsonObject.getJSONObject("main")
            val wind = jsonObject.getJSONObject("wind")

            // City and country name
            val cityName = jsonObject.getString("name")
            Log.d("Main", "It is working $cityName")
            tvName.text = cityName+ ", "+sys.getString("country")

           // Date and time
            val lastUpdate:Long = jsonObject.getLong("dt")
            val lastUpdateText = "Updated at: " + SimpleDateFormat(
                "dd/MM/yyyy hh:mm a",
                Locale.ENGLISH).format(Date(lastUpdate*1000))
            tvTime.text = lastUpdateText

            // Description of weather
            val descp = weather.getString("description")
            tvDescription.text = descp

            // Temp
            val currentTemp = main.getString("temp")
            val temp = try{
                currentTemp.substring(0, currentTemp.indexOf(".")) + "°C"
            }catch(e: Exception){
                currentTemp + "°C"
            }
            tvTemp.text = temp

            // Temp Low and High
            val currentTempLow = main.getString("temp")
            var tempLow = try {
                "Low: " + currentTempLow.substring(0, currentTempLow.indexOf(".")) + "°C"
            } catch (e : Exception){
                currentTempLow + "°C"
            }
            tvLow.text = tempLow

            val currentTempHigh = main.getString("temp_max")

            var tempHigh = try{
                    "High: " + currentTempHigh.substring(0, currentTempHigh.indexOf(".")) + "°C"
            }catch (e:Exception){
                currentTempHigh + "°C"
            }
            tvHigh.text = tempHigh

            // Sunrise and sunset time
            val sunrise:Long = sys.getLong("sunrise")
            tvSunrise.text = "Sunrise"+SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))

            val sunset:Long = sys.getLong("sunset")
            tvSunset.text = "Sunset"+SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))

            // Wind , Pressure , and Humidity
            val windSpeed = wind.getString("speed")
            tvWind.text = windSpeed

            val pressure = main.getString("pressure")
            tvPressure.text = pressure

            val humidity = main.getString("humidity")
            tvHumidity.text = humidity

            // Refresh
            tvRefresh.text = "Refresh data"
            tvRefresh.setOnClickListener{
                city = "52801"
                requestAPI()
                change(1)
            }










        }
    }
}