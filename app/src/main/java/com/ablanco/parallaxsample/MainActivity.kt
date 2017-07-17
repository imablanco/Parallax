package com.ablanco.parallaxsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import android.widget.Toast
import com.ablanco.parallax.ParallaxView
import com.ablanco.parallax.dip
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cbFirstLayerParallax.setOnCheckedChangeListener { _, isChecked ->
            parallaxView.firstLayerAppliesParallax(isChecked)
        }

        var seekProgress = (parallaxView.getParallaxMovementDistance().toFloat() / resources.displayMetrics.density).toInt()
        seekBar.progress = seekProgress
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) seekProgress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                parallaxView.setParallaxMovementDistance(dip(seekProgress).toInt())
                Toast.makeText(this@MainActivity, seekProgress.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        rgTouchMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbLifted -> parallaxView.touchMode = ParallaxView.TOUCH_MODE_LIFTED
                R.id.rbPressed -> parallaxView.touchMode = ParallaxView.TOUCH_MODE_PRESSED
            }
        }
    }
}
