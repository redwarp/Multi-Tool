package net.redwarp.app.multitool.compass

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import net.redwarp.app.multitool.R

class CompassActivity : AppCompatActivity() {
    private val compassListener = CompassListener(this)
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        compassListener.resume()
        compositeDisposable.add(
                compassListener.getAngle().subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            compass.setAngle(it, true)
                        }))
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
        compassListener.pause()
    }
}
