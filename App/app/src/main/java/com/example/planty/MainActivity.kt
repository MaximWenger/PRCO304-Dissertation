package com.example.planty

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseUser



class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button_register.setOnClickListener {
            registerUser()
        }

        alreadyhaveaccount_textview_register.setOnClickListener {
            changeActivityToLogin()
        }
    }

    private fun changeActivityToLogin() {
        Log.d("MainActivity", "Trying to Log in")
        //Launch the login activity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun registerUser(){

        val email = email_edittext_register.text.toString() //Get the user email
        val password = password_edittext_register.text.toString() //Get the user password

        if (email.isEmpty() || password.isEmpty()){ //If both fields are empty, display error message to user
            Toast.makeText(this, "Please enter an email and password", Toast.LENGTH_SHORT).show()
            return
        }
        else if (email.isEmpty()) { //If the email is empty, display error message to user
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            return
        } else if (password.isEmpty()){ //If the password is empty, display error message to user
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("MainActivity", "Email is:" + email) //DEBUG
        Log.d("MainActivity", "Password: $password") //DEBUG


        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                Log.d("Main", "Successfully created user with uid: ${it.result!!.user.uid}")
            }
            .addOnFailureListener{
                Log.d("Main", "Failed to create user ${it.message}")
                Toast.makeText(this, "Failed to create. ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
