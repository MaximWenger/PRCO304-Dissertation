package com.example.planty

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() { //Combatability Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        button_login_login.setOnClickListener {
            val email = edittext_email_login.text.toString()
            val password = edittest_password_login.text.toString()
        }

        textview_backtoregistration_login.setOnClickListener {
            Log.d("MainActivity", "Returning to Registration activity")

            val intent = Intent(this,  MainActivity::class.java)
            startActivity(intent)
        }


    }
}