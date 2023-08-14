package com.example.connectionsmanagement

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.coorchice.library.SuperTextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //跳转至关系图展示界面
        val buttonToResult:Button=findViewById(R.id.ToResult)
        buttonToResult.setOnClickListener{
            val intent=Intent(this,ResultActivity::class.java)
            startActivity(intent)
        }
    }
}