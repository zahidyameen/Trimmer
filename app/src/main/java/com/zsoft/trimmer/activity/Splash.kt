package com.zsoft.trimmer.activity

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zsoft.trimmer.R


class Splash : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                hasPermissionFor33()
            } else {
                hasPermissionForOther()
            }
        }, 2000);
    }

    private fun hasPermissionForOther() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED&&
            ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val intent: Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent: Intent = Intent(this, PermissionsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun hasPermissionFor33() {
        if (ContextCompat.checkSelfPermission(this, READ_MEDIA_AUDIO)
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val intent: Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent: Intent = Intent(this, PermissionsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}