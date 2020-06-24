package com.example.flightmobileapp
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_flight_app.*
import okhttp3.*
import java.io.IOException
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.coroutines.*
class FlightAppActivity : AppCompatActivity() {
    private var isTouchingJoystick: Boolean = false
    private var client = Client(this)
    var oldAileron: Float = 0F; private set
    var oldElevator: Float = 0F; private set
    var oldRudder: Float = 0F; private set
    var oldThrottle: Float = 0F; private set
    var url :String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_app)
        url = intent.getStringExtra("url").toString()
        client.getImage(simulatorScreen, url)
        setJoystickListeners()
        setSeekBarListeners()
    }
    fun setJoystickListeners() {
        joystick.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val action = event!!.action
                //val touchX = event.x -47F
                //val touchY = 2364.5F- event.y
                val touchX = event.x
                val touchY = event.y
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        // if the touch happened outside the joystick then ignore it
                        if (!isInsideJoystick(touchX, touchY)) {
                            return false
                        }
                        // otherwise, update the flag for upcoming move actions
                        isTouchingJoystick = true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (!isTouchingJoystick) {
                            return false
                        }
                        // used to normalize the values
                        val distance = distance(touchX, touchY, joystick.centerX, joystick.centerY)
                        var magnitude = (distance + joystick.innerRadius) / joystick.outerRadius
                        if (magnitude >= 1) {
                            magnitude = 1F
                        }

                        // calculate the values of the arguments and send them
                        val angle = getAngle(
                            (touchX - joystick.centerX).toDouble(),
                            (touchY - joystick.centerY).toDouble()
                        )
                        val elevator: Float = (sin(Math.toRadians(angle)) * magnitude * -1).toFloat()
                        val aileron: Float = (cos(Math.toRadians(angle)) * magnitude).toFloat()
                        // draw the new position
                        val newPos = getAdjustedPosition(touchX, touchY, angle, distance)
                        updateJoystickPosition(newPos[0], newPos[1])
                        filtrateInsufficientMoment(elevator, aileron);
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // place both the joystick and the aircraft's steering handles in the center
                        updateJoystickPosition(joystick.centerX, joystick.centerY)
                        //this.client.sendCommand("elevator","0")
                        //this.client.sendCommand("aileron","0")
                        isTouchingJoystick = false
                    }
                }
                joystick.invalidate()
                return true
            }
        })
        }
    private fun isInsideJoystick(touchX: Float, touchY: Float): Boolean {
        return this.distance(touchX, touchY, joystick.currX, joystick.currY) <= joystick.innerRadius
    }
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }
    private fun getAngle(dx: Double, dy: Double): Double {
        if (dx >= 0 && dy >= 0) return Math.toDegrees(Math.atan(dy / dx))
        else if (dx < 0 && dy >= 0) return Math.toDegrees(Math.atan(dy / dx)) + 180
        else if (dx < 0 && dy < 0) return Math.toDegrees(Math.atan(dy / dx)) + 180
        else if (dx >= 0 && dy < 0) return Math.toDegrees(Math.atan(dy / dx)) + 360
        return 0.0
    }
    private fun getAdjustedPosition(touchX: Float, touchY: Float, angle: Double, distanceFromCenter: Float): Array<Float> {
        if (distanceFromCenter + joystick.innerRadius <= joystick.outerRadius) {
            return arrayOf(touchX, touchY)
        }
        val newX = joystick.centerX + cos(Math.toRadians(angle)) * (joystick.outerRadius - joystick.innerRadius)
        val newY = joystick.centerY + sin(Math.toRadians(angle)) * (joystick.outerRadius - joystick.innerRadius)
        return arrayOf(newX.toFloat(), newY.toFloat())
    }
    private fun updateJoystickPosition(newX: Float, newY: Float) {
        joystick.currX = newX
        joystick.currY = newY
    }
    private fun filtrateInsufficientMoment(elevator: Float, aileron: Float) {
        if (oldAileron * 1.01 < aileron || oldAileron * 0.99 > aileron || oldElevator * 1.01 < elevator || oldElevator * 0.99 > elevator) {
            client.sendControlsValues(url, aileron, elevator, oldRudder, oldThrottle);
            oldAileron = aileron;
            oldElevator = elevator;
        }
    }
    private fun setSeekBarListeners() {
        rudderSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val rudder = progress.toFloat()/100;
                //all changes will be bigger then 1% off the value, no need to check.
                client.sendControlsValues(url, oldAileron, oldElevator, rudder, oldThrottle);
                oldRudder = rudder
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        throttleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //all changes will be bigger then 1% off the value, no need to check.
                val throttle = progress.toFloat()/100;
                client.sendControlsValues(url, oldAileron, oldElevator, oldRudder, throttle);
                oldThrottle = throttle
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        client.stopClient()
    }
}