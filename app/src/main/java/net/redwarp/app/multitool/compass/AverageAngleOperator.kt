package net.redwarp.app.multitool.compass

import io.reactivex.ObservableOperator
import io.reactivex.Observer
import io.reactivex.disposables.Disposable


class AverageAngleOperator(private val valueCount: Int) : ObservableOperator<Float, Float> {
    private var count = 0
    private var offset = 0
    private val values: MutableList<Float> = mutableListOf()

    override fun apply(observer: Observer<in Float>): Observer<in Float> {
        return object : Observer<Float> {
            override fun onComplete() {
                observer.onComplete()
            }

            override fun onError(e: Throwable) {
                observer.onError(e)
            }

            override fun onSubscribe(d: Disposable) {
                observer.onSubscribe(d)
            }

            override fun onNext(t: Float) {
                observer.onNext(computeNext(t))
            }
        }
    }

    private fun computeNext(t: Float): Float {
        if (values.size > offset) {
            values[offset] = t
        } else {
            values.add(t)
        }
        count = minOf(count + 1, valueCount)
        offset = (offset + 1) % valueCount
        val workList = values.subList(0, count)
        val min: Float = workList.min() ?: return 0f
        return workList.map {
            var normalized = it - min
            if (Math.abs(2 * Math.PI - normalized) > normalized) {
                normalized = (normalized + 2 * Math.PI).toFloat()
            }
            normalized
        }.sum() / count + min
    }
}
