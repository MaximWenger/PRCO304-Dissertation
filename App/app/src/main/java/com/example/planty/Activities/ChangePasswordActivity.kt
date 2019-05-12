package com.example.planty.Activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.planty.Classes.ActivityNavigation
import com.example.planty.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class ChangePasswordActivity : AppCompatActivity() {
    private var userEmail = ""
    private var correctCurrentPassword = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        supportActionBar?.title = "Planty  |  Change Password"
        populateUserEmail()//populate global var  with user email

        ChangePasswordActivity_Button.setOnClickListener{
            hideKeyboard()
            reAuthUser()
            changeUserPassword()
        }
        ChangePasswordActivity_TextView_NewPassword.setOnClickListener{
            reAuthUser()
        }

    }

    /**
     * Calls methods the check the user details are correct, then calls changeUserPasswordInFirebase()
     */
    private fun changeUserPassword(){
        val newPassword1 = ChangePasswordActivity_TextView_NewPassword.text.toString()
        val newPassword2 = ChangePasswordActivity_TextView_NewPasswordAgain.text.toString()
        val currentPassword = ChangePasswordActivity_TextView_CurrentPassword.text.toString()

        if (checkUserFieldsNotEmpty(newPassword1, newPassword2, currentPassword)) {
            if (checkUserInput(newPassword1, newPassword2)) {
                changeUserPasswordInFirebase(newPassword1)
            }
        }

    }

    /**
     * Changes the user password in Firebase
     * @param newPassword New User Password
     */
    private fun changeUserPasswordInFirebase(newPassword: String){
        Log.d("ChangePasswordActivity", "Trying to save new password")
        try {
            if (correctCurrentPassword == true) {
                var user = FirebaseAuth.getInstance().currentUser
                user?.updatePassword(newPassword)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password saved!", Toast.LENGTH_SHORT).show()
                            Log.d("ChangePasswordActivity", "User password updated.")
                        } else if (task.isCanceled) {
                            Log.d("ChangePasswordActivity", "Password change cancelled")
                        }
                    }
            }
            else {
                Toast.makeText(this, "Incorrect current password.", Toast.LENGTH_SHORT).show()
            }
        }
        catch(e: Exception){
            Log.d("ChangePasswordActivity", "Something went wrong = ${e.message}")
        }
    }

    /**
     * Reauthenticaes the user with Firebase, using the Account email and User entered current password
     * Needed for password change to work changeUserPasswordInFirebase()
     */
    private fun reAuthUser(){
        try {
            val currentPassword = ChangePasswordActivity_TextView_CurrentPassword.text.toString()
            var user = FirebaseAuth.getInstance().currentUser
            val credential = EmailAuthProvider
                .getCredential(userEmail, currentPassword)//Needs email and password
            user?.reauthenticate(credential)
                ?.addOnCompleteListener { Log.d("ChangePasswordActivity", "User re-authenticated.") }
                ?.addOnFailureListener {
                    Log.d("ChangePasswordActivity", "User could not be re-authenticated.")
                    correctCurrentPassword = false
                }
                ?.addOnSuccessListener {
                    correctCurrentPassword = true
                    Log.d("ChangePasswordActivity", "Current password correct! Can re-auth")
                }
        }
        catch(e:Exception){
            Log.d("ChangePasswordActivity", "reAuthUser Something went wrong ${e.message}")
        }
    }

    /**
     * Populates Global variable with the current user Email from Firebase
     */
    private fun populateUserEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            userEmail = user.email.toString()
        }
    }

    /**
     * Checks that the user fields are not empty
     * @param newPassword1 New Password
     * @param newPassword2 New Password again
     * @return Boolean If the fields are empty or not
     */
    private fun checkUserFieldsNotEmpty(newPassword1: String?, newPassword2: String?, currentPassword: String?):Boolean{
        if (newPassword1!!.isEmpty() && newPassword2!!.isEmpty() && currentPassword!!.isEmpty()){
            Toast.makeText(this, "Please enter your current password and new passwords", Toast.LENGTH_SHORT).show()
            return false
        }else if (newPassword1!!.isEmpty() && newPassword2!!.isEmpty()){
            Toast.makeText(this, "Please enter your new Password in both fields", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (newPassword2!!.isEmpty() && currentPassword!!.isEmpty()){
            Toast.makeText(this, "Please enter your current password and new password", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (newPassword1!!.isEmpty() && currentPassword!!.isEmpty()){
            Toast.makeText(this, "Please enter your new password and current password", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (newPassword1!!.isEmpty()){
            Toast.makeText(this, "Please enter your new Password", Toast.LENGTH_SHORT).show()
            return false
        }else if(newPassword2!!.isEmpty()){
            Toast.makeText(this, "Please enter your new Password Again", Toast.LENGTH_SHORT).show()
            return false
        }else if(currentPassword!!.isEmpty()){
            Toast.makeText(this, "Please enter your current password", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    /**
     * Checks that the new user passwords are identical and >=6 chars long
     * @param newPassword1 New password
     * @param newPassword2 New password again
     * @return Boolean, if it is correct
     */
    private fun checkUserInput(newPassword1: String, newPassword2: String):Boolean{
      if (newPassword1 == newPassword2){
         if (newPassword1.length >= 6){
             return true
         }
          Toast.makeText(this, "Your Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
          return false
    }
        Toast.makeText(this, "New Passwords do not match.", Toast.LENGTH_SHORT).show()
        return false
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
                ActivityNavigation.navToProfileActivity(this) //Go to ProfileActivity
            }
            R.id.nav_Identify -> {
                ActivityNavigation.navToIdentifyActivity(this)
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

    /**Hides the mobile keyboard
     *
     */
    private fun hideKeyboard(){
        try {
            val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.SHOW_FORCED)
        }
        catch(e: Exception){
            Log.d("MapsActivity","Error closing keyboard = ${e.message}")
        }
    }

}
