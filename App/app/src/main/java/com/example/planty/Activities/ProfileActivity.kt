package com.example.planty.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.planty.Classes.ActivityNavigation
import com.example.planty.Classes.DisplayAllUserIdents
import com.example.planty.Entities.Identified
import com.example.planty.Entities.User
import com.example.planty.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    private var allUserIdentifications: MutableList<Identified> = mutableListOf<Identified>() //Populated by getLatestIdentifications() to store all user identifications for user search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.title = "Planty  |  Profile"
        ActivityNavigation.verifyLoggedIn(this)//check the User is logged in
        getUserData()
        userData()
        setButtonListeners()
        getAllIdents()


    }

    /**
     * Populates the recyleview with previous user Identifications
     */
    private fun populateRecyclerView(){
        try {
            val recyclerView = findViewById(R.id.ActivityProfile_PreviousIdents) as RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            val idents = allUserIdentifications
            val adapter = DisplayAllUserIdents(idents)
            recyclerView.adapter = adapter
        }
        catch(e:java.lang.Exception){
            Log.d("ProfileActivity", "populateRecyclerView() Something went wrong ${e.message}")
        }
    }

    /**
     * Sets the buttonOnClick Listeners
     */
    private fun setButtonListeners(){
        ProfileActivity_SignOut_Button.setOnClickListener{
            ActivityNavigation.signOut(this)
        }
        ProfileActivity_ChangePassword_Button.setOnClickListener{
            changeToPasswordActivity()
        }
    }

    /**
     * Changes to the Change Password activity
     */
    private fun changeToPasswordActivity(){
        val intent = Intent(this, ChangePasswordActivity::class.java) //Populate intent with new activity class
        this.startActivity(intent) //Change to new class
    }

    /**
     * Populates global var allUserIdentifications with every identification the user has made
     */
    private fun getAllIdents(){
        try {
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/identifiedPlants/${uid}")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach {
                        val userIdentified = (it.getValue(Identified::class.java))
                        userIdentified?.let { it1 -> allUserIdentifications?.add(it1) }
                    }
                    populateRecyclerView()
                    Log.d("ProfileActivity", " allUserIdents size= ${allUserIdentifications.size}")
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d("ProfileActivity", "getLatestIdentifications Error = ${p0.message}")
                }
            })
        }
        catch(e:Exception){
            Log.d("ProfileActivity", "getAllIdents() Something went wrong ${e.message}")
        }

    }

    /**Gets user data, username and join date/time
     *
     */
    private fun getUserData() {
        try {
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = (p0.getValue(User::class.java))
                    populateUserName(user!!.username)
                    populateUserJoinDate(user.dateTime)

/*                p0.children.forEach {
                    Log.d("profileActivity", it.toString())
                    ProfileActivity_TextView_UserEmail.text = FirebaseAuth.getInstance().currentUser?.email
                    if (it.key.toString() == "username"){
                        //userName =


                    }
                }*/
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d("profileActivity", "Error getting user data")
                }
            })
        }
        catch(e:Exception){
            Log.d("profileActivity", "getUserData() Error = ${e.message} ")
        }
    }

    /**
     * Populates textfield with users username
     * @param userName Users username
     */
    private fun populateUserName(userName: String?){
        profile_userUsername.text = userName.toString()
    }

    /**
     * Populates textfield with user email
     * @param email Users email
     */
    private fun populateUserEmail(email: String?){
        ProfileActivity_TextView_UserEmail.text = email.toString()
    }

    /**
     * Populates textfield with user Join date/Time
     * @param joinDate The date the user made the account
     */
    private fun populateUserJoinDate(joinDate: String?){
        profileActivity_TextView_JoinDate.text = joinDate.toString()
    }

    /**
     * Returns user email from Firebase and calls populateUserEmail()
     */
    private fun userData() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = user.email
            populateUserEmail(email)
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
               ActivityNavigation.navToContactUsActivity(this)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

}
