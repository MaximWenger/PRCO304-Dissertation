package com.example.planty

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() { //Combatability Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        button_login_login.setOnClickListener {
            val email = edittext_email_login.text.toString()
            val password = edittest_password_login.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(it.isSuccessful){ //If the login is successful
                        Log.d("Login", "Successfully logged in : ${it.result!!.user.uid}")
                        Toast.makeText(this, "Logged in!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener{ //If the login fails
                    Log.d("Login", "Failed to log in ${it.message}")
                    Toast.makeText(this, "Failed to log in. ${it.message}", Toast.LENGTH_SHORT).show()

                }
        }

        textview_backtoregistration_login.setOnClickListener {
            Log.d("MainActivity", "Returning to Registration activity")

            val intent = Intent(this,  MainActivity::class.java)
            startActivity(intent)
        }


    }
}