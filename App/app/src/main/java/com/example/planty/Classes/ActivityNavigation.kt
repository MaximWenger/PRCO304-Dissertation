package com.example.planty.Classes

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.example.planty.Activities.*
import com.google.firebase.auth.FirebaseAuth

class ActivityNavigation {
    companion object {
        fun navToProfileActivity(c: Context) {//Cannot be "this" as it's reserved
            val intent = Intent(c, ProfileActivity::class.java) //Populate intent with new activity class
            c.startActivity(intent) //Change to new class
        }

        fun navToContactUsActivity(c: Context) {
            val intent = Intent(c, ContactUsActivity::class.java) //Populate intent with new activity class
            c.startActivity(intent) //Change to new class
        }

        /**Sings the user out and returns to the login screen
         *
         */
        fun signOut(c: Context) {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(c, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            c.startActivity(intent)
        }

        /**Changes activity to IdentifyActivity
         *
         */
        fun navToIdentifyActivity(c: Context) {
            val intent = Intent(c, IdentifyActivity::class.java) //Populate intent with new activity class
            c.startActivity(intent) //Change to new class
        }

        /**Checks the user is logged in, returns to Login if not logged in
         *
         */
        fun verifyLoggedIn(c: Context) { //Check if the User is already logged in, if not, return User to registerActivity
            val uid = FirebaseAuth.getInstance().uid
            if (uid == null) {
                val intent = Intent(c, RegisterActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
                c.startActivity(intent)
            }
        }

        fun navToMapsActivity(c: Context) {
            val intent = Intent(c, MapsActivity::class.java) //Populate intent with new activity class
            c.startActivity(intent) //Change to new class
        }

    }


}
