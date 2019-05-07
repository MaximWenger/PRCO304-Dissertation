package com.example.planty.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.planty.Classes.ActivityNavigation
import com.example.planty.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        ActivityNavigation.verifyLoggedIn(this)//check the User is logged in
        getUserData()
        userData()
    }

    /**Gets user data
     *
     */
    private fun getUserData() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    Log.d("profileActivity", it.toString())
                    ProfileActivity_TextView_UserEmail.text = FirebaseAuth.getInstance().currentUser?.email
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("profileActivity", "Error getting user data")
            }
        })
    }

    private fun userData() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl

            // Check if user's email is verified
            val emailVerified = user.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = user.uid

            Log.d(
                "profileActivity",
                "User data = Name = $name, Email = $email, photoURL = $photoUrl, Email verified = $emailVerified, UID = $uid"
            )
        }
    }

    /**Inflates the options menu in the top right
     *
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Create the menu
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**Calls methods when a specific menu option is selected
     *
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean { //When an option from the menu is clicked
        when (item?.itemId) { //Switch statement
            R.id.nav_Profile -> { //DOES NOTHING RIGHT NOW
                return super.onOptionsItemSelected(item)  //Return as already within Identify Activity
            }
            R.id.nav_Identify -> {
                ActivityNavigation.navToIdentifyActivity(this)//Go to Identify Activity
            }
            R.id.nav_Home -> {
                ActivityNavigation.navToMapsActivity(this) //Go to MapsActivity
            }
            R.id.nav_Sign_Out -> {
                ActivityNavigation.signOut(this) //Signs the User out and returns to RegisterActivity
            }
            R.id.nav_Contact -> { //DOES NOTHING RIGHT NOW
                return super.onOptionsItemSelected(item)  //
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

}
