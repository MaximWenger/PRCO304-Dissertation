package com.example.planty

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth

class IdentifyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identify)

        supportActionBar?.title = "Planty  |  Find Plants"

        verifyLoggedIn()//check the user is logged in

    }

    private fun verifyLoggedIn(){ //Check if the user is already logged in, if not, return user to registerActivity
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent =  Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //Create the menu
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean { //When an option from the menu is clicked
        when (item?.itemId){ //Switch statement
            R.id.nav_Profile -> { //DOES NOTHING RIGHT NOW
                return super.onOptionsItemSelected(item)  //
            }
            R.id.nav_Identify -> { //DOES NOTHING RIGHT NOW
                return super.onOptionsItemSelected(item)  //
            }
            R.id.nav_Find -> { //DOES NOTHING RIGHT NOW
                return super.onOptionsItemSelected(item)  //
            }
            R.id.nav_Sign_Out -> {
                signOut() //Signs the user out and returns to RegisterActivity
            }
            R.id.nav_Contact -> { //DOES NOTHING RIGHT NOW
                return super.onOptionsItemSelected(item)  //
            }
            else ->  return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
 //CURRENTLY AT 14:59, ADDING A META TAG TO THE MANIFEST TO ADD A BACK BUTTON IN THE RIGHT AREA