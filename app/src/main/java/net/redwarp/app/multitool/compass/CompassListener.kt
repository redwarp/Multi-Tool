package net.redwarp.app.multitool.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class CompassListener(val context: Context) : SensorEventListener {
    companion object {
        private fun angleMod(value: Float): Float {
            var workValue = value;
            if (workValue < 0) {
                workValue = value + 360f
            }
            return workValue
        }
    }

    private val publisher: PublishSubject<Float> = PublishSubject.create()
    private val sensorManager: SensorManager
        get() = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun getAngle(): Observable<Float> = publisher.debounce(16, TimeUnit.MILLISECONDS)
            .lift(AverageAngleOperator(8))
            .distinct()
            .map {
                angleMod((-it * 360f / (2f * Math.PI)).toFloat())
            }

    fun resume() {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magneticField,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
    }

    fun pause() {
        sensorManager.unregisterListener(this)
    }

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var azimut: Float = 0f
    private val orientation = FloatArray(3)
    private val R = FloatArray(9)
    private val I = FloatArray(9)

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == null) {
            return
        }

        if (event.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values
        }
        if (event.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values
        }
        if (gravity != null && geomagnetic != null) {

            val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)
            if (success) {
                SensorManager.getOrientation(R, orientation)
                azimut = orientation[0] // orientation contains: azimut, pitch and roll

                publisher.onNext(azimut)
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}
