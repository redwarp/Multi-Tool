package net.redwarp.app.multitool.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class CompassListener(private val context: Context) : SensorEventListener {
    companion object {
        private fun angleMod(value: Float): Float {
            var workValue = value
            if (workValue < 0) {
                workValue = value + 360f
            }
            return workValue
        }
    }

    private val publisher: PublishSubject<Float> = PublishSubject.create()
    private val sensorManager: SensorManager
        get() = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun getAngle(): Observable<Float> = publisher
            .debounce(16, TimeUnit.MILLISECONDS)
            .lift(AverageAngleOperator(1))
            .distinct()
            .map {
                angleMod((-it * 360f / (2f * Math.PI)).toFloat())
            }

    fun resume() {
        val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)
    }

    fun pause() {
        sensorManager.unregisterListener(this)
    }

    private val matrixFromVector = FloatArray(9)
    private val remapedMatrix = FloatArray(9)
    private val values = FloatArray(3)

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == null) {
            return
        }
        if (event.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(matrixFromVector, event.values)
            SensorManager.remapCoordinateSystem(matrixFromVector, SensorManager.AXIS_Z, SensorManager.AXIS_Y, remapedMatrix)
            SensorManager.getOrientation(remapedMatrix, values)

            publisher.onNext(values[0])
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}
