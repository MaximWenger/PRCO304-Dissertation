package com.example.planty.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.planty.R
import com.google.firebase.auth.FirebaseAuth

class IdentifiedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identified)

        verifyLoggedIn()

        val test = intent.getStringArrayListExtra("identifications")
        Log.d("IdentifiedActivity", "test = ${test.size}")
        for (item in test){
            Log.d("IdentifiedActivity", "test = ${item}")
        }
        Log.d("IdentifiedActivity", "COMPLETED")
    }

    private fun verifyLoggedIn(){ //Check if the User is already logged in, if not, return User to registerActivity
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent =  Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
            startActivity(intent)
        }
    }
}
