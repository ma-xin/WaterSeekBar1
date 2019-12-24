package com.example.waterseekbar

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.waterseekbar.widget.WaterSeekBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()

    }

    private fun initView() {
        val bar1 : WaterSeekBar = findViewById(R.id.seekBar1)
        val bar2 : WaterSeekBar = findViewById(R.id.seekBar2)
        val bar3 : WaterSeekBar = findViewById(R.id.seekBar3)
        val bar4 : WaterSeekBar = findViewById(R.id.seekBar4)
        bar4.setLineColors(
            Color.parseColor("#2AA703"),
            Color.parseColor("#3F51B5"),
            Color.parseColor("#F39A0F"),
            Color.parseColor("#673AB7"),
            Color.parseColor("#D81B60"))
        bar1.setProgressChangeListener {
            Toast.makeText(this, "WaterSeekBar1 progress: ${it}", Toast.LENGTH_SHORT).show()
        }
        bar2.setProgressChangeListener {
            Toast.makeText(this, "WaterSeekBar2 progress: ${it}", Toast.LENGTH_SHORT).show()
        }
        bar3.setProgressChangeListener {
            Toast.makeText(this, "WaterSeekBar3 progress: ${it}", Toast.LENGTH_SHORT).show()
        }
        bar4.setProgressChangeListener {
            Toast.makeText(this, "WaterSeekBar4 progress: ${it}", Toast.LENGTH_SHORT).show()
        }
    }
}
