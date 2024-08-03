package com.ajithvgiri.bell

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ajithvgiri.bell.databinding.ActivityMainBinding
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    companion object{
        private const val SHAKE_THRESHOLD = 200
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7f
    }

    var sensorManager: SensorManager? = null
    var accelerometer: Sensor? = null
    lateinit var soundPool:SoundPool
    private var soundID = 0
    private var mShakeTimestamp: Long = 0
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)


        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder().setMaxStreams(10).build()
        } else {
            SoundPool(10, AudioManager.STREAM_MUSIC, 1)
        }
        soundID = soundPool.load(this, R.raw.sound, 1)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this, accelerometer)
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH
            // gForce will be close to 1 when there is no movement.
            val gForce: Float = sqrt(gX * gX + gY * gY + gZ * gZ)
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                val now = System.currentTimeMillis()
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_THRESHOLD > now) {
                    return
                }
                mShakeTimestamp = now
                playSound()
            }
        }
    }

    private fun playSound(){
        binding.animationView.playAnimation()
        soundPool.play(soundID,1F, 1F, 1, 0, 1F)
    }
}
