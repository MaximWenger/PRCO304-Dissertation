package com.example.planty

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)





        register_button_register.setOnClickListener {

            val email = email_edittext_register.text.toString()
            var password = password_edittext_register.text.toString()

            Log.d("MainActivity", "Email is:" + email)
            Log.d("MainActivity", "Password: " + password)

            //GOT TO 23.55 in the video
        }

        alreadyhaveaccount_textview_register.setOnClickListener {
            Log.d("MainActivity", "Trying to Log in")

            //Launch the login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
