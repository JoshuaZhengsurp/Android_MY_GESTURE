package com.example.mygesture

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mygesture.gesture.GestureActivity
import com.example.mygesture.manager.AppManager

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContentView(R.layout.activity_main)
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    AppManager.getInstance(this);

    val btnDraw = findViewById<Button>(R.id.btn_draw)
    btnDraw.setOnClickListener {
      val intent = Intent(this, DrawActivity::class.java)
      startActivity(intent)
    }

    val btnGesture = findViewById<Button>(R.id.btn_gesture)
    btnGesture.setOnClickListener {
      val intent = Intent(this, GestureActivity::class.java)
      startActivity(intent)
    }

//    val btnOpenTest = findViewById<Button>(R.id.btn_open_app)
//    btnOpenTest.setOnClickListener {
//      val intent = Intent(this, AppSelectionActivity::class.java)
//      startActivity(intent)
//    }
  }
}
