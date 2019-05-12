package com.example.planty.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.planty.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception

class LoginActivity : AppCompatActivity() { //Combatability Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        button_login_login.setOnClickListener {
            hideKeyboard()
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

        if(checkUserfields(email, password)) {
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
    }

    /**
     * Checks the user Password and Emails fields are populated
     * @param email User Email
     * @param password user Password
     */
    private fun checkUserfields(email: String?, password: String?): Boolean {
        if (email!!.isEmpty() && password!!.isEmpty()){
            Toast.makeText(this, "Please enter Email and Password", Toast.LENGTH_SHORT).show()
            return false
        }else if (email!!.isEmpty()){
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return false
        }else if(password!!.isEmpty()){
            Toast.makeText(this, "Please enter your Password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**Changes to RegisterActivity
     *
     */
    private fun changeActivitytoRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    /**
     * Hides the user keyboard
     */
    private fun hideKeyboard(){
        try {
            val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.SHOW_FORCED)
        }
        catch(e: Exception){
            Log.d("RegisterActivity","Error closing keyboard = ${e.message}")
        }
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