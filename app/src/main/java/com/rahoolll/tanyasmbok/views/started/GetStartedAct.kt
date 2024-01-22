package com.rahoolll.tanyasmbok.views.started

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import com.rahoolll.tanyasmbok.R
import com.rahoolll.tanyasmbok.databinding.ActivityGetStartedBinding
import com.rahoolll.tanyasmbok.views.main.MainActivity

class GetStartedAct : AppCompatActivity() {
    private lateinit var binding: ActivityGetStartedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        with(binding){
            btnChat.setOnClickListener{
                val intent = Intent(this@GetStartedAct, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}