package com.example.planty.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.planty.Classes.ActivityNavigation
import com.example.planty.R
import com.google.firebase.auth.FirebaseAuth

class ContactUsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        supportActionBar?.title = "Planty  |  Contact Us"

        ActivityNavigation.verifyLoggedIn(this)
    }


    /**Inflates the Options menu in the top right of the activity
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
            R.id.nav_Profile -> {
                ActivityNavigation.navToProfileActivity(this)
            }
            R.id.nav_Identify -> {
               ActivityNavigation.navToIdentifyActivity(this)
            }
            R.id.nav_Home -> {
               ActivityNavigation.navToMapsActivity(this)
            }
            R.id.nav_Sign_Out -> {
               ActivityNavigation.signOut(this)
            }
            R.id.nav_Contact -> {
                return super.onOptionsItemSelected(item)  //
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }


}
