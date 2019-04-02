package com.example.planty.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.planty.R
import com.example.planty.classes.DateTime
import com.example.planty.classes.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*


class RegisterActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button_register.setOnClickListener {
            Log.d("RegisterActivity", "Register button pressed")
            registerUser()
        }

        alreadyhaveaccount_textview_register.setOnClickListener {
            changeActivityToLogin()
        }
    }

    private fun changeActivityToLogin() {//Change activity to the login activity
        Log.d("RegisterActivity", "Trying to Log in")
        //Launch the login activity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun registerUser(){//Attempt to register the new User details with Firebase
        val username = username_edittext_register.text.toString() //Get the username
        val email = email_edittext_register.text.toString() //Get the User email
        val password = password_edittext_register.text.toString() //Get the User password

        if (email.isEmpty() || password.isEmpty()){ //If both fields are empty, display error message to User
            Toast.makeText(this, "Please enter an email and password", Toast.LENGTH_SHORT).show()
            return
        }
        else if (email.isEmpty()) { //If the email is empty, display error message to User
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            return
        } else if (password.isEmpty()){ //If the password is empty, display error message to User
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is:" + email) //DEBUG
        Log.d("RegisterActivity", "Password: $password") //DEBUG


        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                Log.d("RegisterActivity", "Successfully created User with uid: ${it.result!!.user.uid}")
                saveUserToDataBase(username)
            }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Failed to create User ${it.message}")
                Toast.makeText(this, "Failed to create. ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun changeActivityToHome(){
        val intent = Intent(this, IdentifyActivity::class.java) //Populate intent with new activity class
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

    private fun saveUserToDataBase(Username: String){
        Log.d("RegisterActivity", "Trying to save User") //Debug
        val role = "user"
        val uid = FirebaseAuth.getInstance().uid ?: "" //Elvis operator //Using the unique ID (Used to link authenticated User to the database within Firebase
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")//Using the unique ID (Used to link authenticated User to the database within Firebase) as a unique name within Firebase

        val dateTime = DateTime().getDateTime()

        val user = User(Username, role, dateTime) //Creating new User object, to populate Firebase with

        ref.setValue(user).addOnSuccessListener { //Saving the User object to the Firebase database.
            Log.d("RegisterActivity", "Saved username and unique identifer to the database")

            changeActivityToHome() //Change actvity to homeactivity
        }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Something went wrong ${it.message}")
            }
    }






}

