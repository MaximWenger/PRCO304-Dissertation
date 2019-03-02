package com.example.planty

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button_register.setOnClickListener {

            val email = email_edittext_register.text.toString()
            val password = password_edittext_register.text.toString()

            Log.d("MainActivity", "Email is:" + email)
            Log.d("MainActivity", "Password: $password")

            //Firebase authentication
/*            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener //If it fails, return


                //else if successful
               Log.d("Main", "Successfully created user with uid: ${it.result.user.uid}")
            }*/

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful){
                        Log.d("Main", "CreateUserWithEmail: success")
                        val user = FirebaseAuth.getInstance().currentUser
                       Log.d("Main", "" + user)
                    }else{
                        //If signin fails
                        Log.d("Main", "Sign in failed", task.exception)

                    }
                }

        }

        alreadyhaveaccount_textview_register.setOnClickListener {
            Log.d("MainActivity", "Trying to Log in")

            //Launch the login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
