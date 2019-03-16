package com.example.planty

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


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

    private fun changeActivityToLogin() {//Change activity to the login activity
        Log.d("RegisterActivity", "Trying to Log in")
        //Launch the login activity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun registerUser(){//Attempt to register the new user details with Firebase
        val username = username_edittext_register.text.toString() //Get the username
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

        Log.d("RegisterActivity", "Email is:" + email) //DEBUG
        Log.d("RegisterActivity", "Password: $password") //DEBUG


        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                Log.d("RegisterActivity", "Successfully created user with uid: ${it.result!!.user.uid}")
                saveUserToDataBase(username)
            }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Failed to create user ${it.message}")
                Toast.makeText(this, "Failed to create. ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToDataBase(Username: String){
        Log.d("RegisterActivity", "Trying to save user") //Debug

        val uid = FirebaseAuth.getInstance().uid ?: "" //Elvis operator //Using the unique ID (Used to link authenticated user to the database within Firebase
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")//Using the unique ID (Used to link authenticated user to the database within Firebase) as a unique name within Firebase

        val user = User(Username, uid) //Creating new user object, to populate Firebase with

        ref.setValue(user).addOnSuccessListener { //Saving the user object to the Firebase database.
            Log.d("RegisterActivity", "Saved username and unique identifer to the database")
        }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Something went wrong ${it.message}")
            }
    }


}

class User(val username: String, val uid: String)