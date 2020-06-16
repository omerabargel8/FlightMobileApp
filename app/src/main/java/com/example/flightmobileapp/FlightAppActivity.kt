package com.example.flightmobileapp
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_flight_app.*
import okhttp3.*
import java.io.IOException
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class FlightAppActivity : AppCompatActivity() {
    private lateinit var joystickView: JoystickView
    private var isTouchingJoystick: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //this.joystickView = JoystickView(this)
        setContentView(R.layout.activity_flight_app)
        getImage()
        this.joystickView = omer
        //val intent = Intent(this, JoystickActivity::class.java)
        //startActivity(intent)

        //setContentView(this.joystickView)
    }
    fun getImage() {
        val client = OkHttpClient();
        val request: Request = Request.Builder()
            .url("http://10.0.2.2:59754/api/screenshot")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call, response: Response) {
                val I = response?.body()?.byteStream()
                val B = BitmapFactory.decodeStream(I)
                runOnUiThread {
                    simulatorScreen.setImageBitmap(B)
                }
            }
        })
    }

    /**
     * The function is called whenever the user is interacting with the screen.
     * Is responsible for the joystick-moving logic.
     *
     * @param event - the gesture the user acted on the screen
     * @return true if a relevant action was performed, false otherwise
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event!!.action
        val touchX = event.x
        val touchY = event.y

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                // if the touch happened outside the joystick then ignore it
                if (!this.isInsideJoystick(touchX, touchY)) {
                    return false
                }
                // otherwise, update the flag for upcoming move actions
                this.isTouchingJoystick = true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!this.isTouchingJoystick) {
                    return false
                }
                // used to normalize the values
                val distance = this.distance(touchX, touchY, this.joystickView.centerX, this.joystickView.centerY)
                var magnitude = (distance + this.joystickView.innerRadius) / this.joystickView.outerRadius
                if (magnitude >= 1) {
                    magnitude = 1F
                }

                // calculate the values of the arguments and send them
                val angle = this.getAngle(
                    (touchX - this.joystickView.centerX).toDouble(),
                    (touchY - this.joystickView.centerY).toDouble()
                )
                val elevator: Float = (sin(toRadians(angle)) * magnitude * -1).toFloat()
                val aileron: Float = (cos(toRadians(angle)) * magnitude).toFloat()

                //this.client.sendCommand("elevator", elevator.toString())
                //this.client.sendCommand("aileron", aileron.toString())

                // draw the new position
                val newPos = this.getAdjustedPosition(touchX, touchY, angle, distance)
                this.updateJoystickPosition(newPos[0], newPos[1])
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // place both the joystick and the aircraft's steering handles in the center
                this.updateJoystickPosition(this.joystickView.centerX, this.joystickView.centerY)
                //this.client.sendCommand("elevator","0")
                //this.client.sendCommand("aileron","0")
                this.isTouchingJoystick = false
            }
        }
        this.joystickView.postInvalidate()
        return true
    }

    /**
     * Checking if a touch-gesture position is inside the joystick
     *
     * @param touchX - the x coordinate of the touching point
     * @param touchY - the y coordinate of the touching point
     * @return true if the distance from the position to the center is less or equal to the radius, false otherwise
     */
    private fun isInsideJoystick(touchX: Float, touchY: Float): Boolean {
        println("touch x "+touchX)
        println("curx "+ this.joystickView.centerX)
        println("touch y "+(2560-touchY))
        println("cur y "+this.joystickView.centerY)
        println("distance " +distance(touchX-47F, 2364.5F-touchY, this.joystickView.centerX, this.joystickView.centerY))
        println("inner " +this.joystickView.innerRadius)
        return this.distance(touchX -47F, 2364.5F-touchY, this.joystickView.centerX, this.joystickView.centerY) <= this.joystickView.innerRadius
    }

    /**
     * Calculating the distance between two points
     *
     * @param x1 - x coordinate of point 1
     * @param y1 - y coordinate of point 1
     * @param x2 - x coordinate of point 2
     * @param y2 - y coordinate of point 2
     * @return the distance between the points
     */
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }

    /**
     * Calculating the angle between two points using the differences in x and y values
     *
     * @param dx - change in x coordinates between two points
     * @param dy - change in y coordinate between two points
     * @return the angle between the two points
     */
    private fun getAngle(dx: Double, dy: Double): Double {
        if (dx >= 0 && dy >= 0) return Math.toDegrees(Math.atan(dy / dx))
        else if (dx < 0 && dy >= 0) return Math.toDegrees(Math.atan(dy / dx)) + 180
        else if (dx < 0 && dy < 0) return Math.toDegrees(Math.atan(dy / dx)) + 180
        else if (dx >= 0 && dy < 0) return Math.toDegrees(Math.atan(dy / dx)) + 360
        return 0.0
    }

    /**
     * Correcting the touching position if occurred outside the joystick
     *
     * @param touchX             - the x coordinate of the touching point
     * @param touchY             - the y coordinate of the touching point
     * @param angle              - the angle between the touching point and the center of the joystick
     * @param distanceFromCenter - the distance from the center
     * @return the original position if its inside the joystick, otherwise a point on the outer circumference
     */
    private fun getAdjustedPosition(touchX: Float, touchY: Float, angle: Double, distanceFromCenter: Float): Array<Float> {
        if (distanceFromCenter + this.joystickView.innerRadius <= this.joystickView.outerRadius) {
            return arrayOf(touchX, touchY)
        }
        val newX = this.joystickView.centerX + cos(toRadians(angle)) * (joystickView.outerRadius - joystickView.innerRadius)
        val newY = this.joystickView.centerY + sin(toRadians(angle)) * (joystickView.outerRadius - joystickView.innerRadius)
        return arrayOf(newX.toFloat(), newY.toFloat())
    }

    private fun updateJoystickPosition(newX: Float, newY: Float) {
        this.joystickView.currX = newX
        this.joystickView.currY = newY
    }

    override fun onDestroy() {
        //this.client.disconnect()
        super.onDestroy()
    }
}