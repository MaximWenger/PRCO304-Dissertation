package com.example.planty.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.planty.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() { //Combatability Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        button_login_login.setOnClickListener {
            logUserIn()
        }
        textview_backtoregistration_login.setOnClickListener {
            changeActivitytoRegister()
        }
    }

    /**Logs the user in
     *
     */
    private fun logUserIn() {
        val email = edittext_email_login.text.toString()
        val password = edittest_password_login.text.toString()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) { //If the login is successful
                    Toast.makeText(this, "Logged in!", Toast.LENGTH_SHORT).show()
                    changeActivityToHome() //When logged in, change activity to home
                }
            }
            .addOnFailureListener {
                //If the login fails
                Log.d("Login", "Failed to log in ${it.message}")
                Toast.makeText(this, "Failed to log in. ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**Changes to RegisterActivity
     *
     */
    private fun changeActivitytoRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    /**Changes to IdentifyActivity
     *
     */
    private fun changeActivityToHome() {
        val intent = Intent(this, MapsActivity::class.java) //Populate intent with new activity class
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }
}