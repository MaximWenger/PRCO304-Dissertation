package com.example.planty.Activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.planty.R
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

                        changeActivityToHome() //When logged in, change activity to home

                    }
                }
                .addOnFailureListener{ //If the login fails
                    Log.d("Login", "Failed to log in ${it.message}")
                    Toast.makeText(this, "Failed to log in. ${it.message}", Toast.LENGTH_SHORT).show()

                }
        }

        textview_backtoregistration_login.setOnClickListener {
            Log.d("MainActivity", "Returning to Registration activity")

            val intent = Intent(this,  RegisterActivity::class.java)
            startActivity(intent)
        }


    }

    private fun changeActivityToHome(){
        val intent = Intent(this, IdentifyActivity::class.java) //Populate intent with new activity class
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }
}