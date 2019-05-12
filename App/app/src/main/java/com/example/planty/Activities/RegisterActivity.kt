package com.example.planty.Activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.planty.R
import com.example.planty.Classes.DateTime
import com.example.planty.Entities.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception


class RegisterActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setOnClickListeners()
    }

    /**
     * creates onClickListeners for the buttons
     */
    private fun setOnClickListeners(){
        register_button_register.setOnClickListener {
            Log.d("RegisterActivity", "Register button pressed")
            hideKeyboard()
            registerUser()
        }

        alreadyhaveaccount_textview_register.setOnClickListener {
            changeActivityToLogin()
        }
    }

    /**Change to Login activity
     *
     */
    private fun changeActivityToLogin() {//Change activity to the login activity
        Log.d("RegisterActivity", "Trying to Log in")
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /**Register the user details with Firebase
     *
     */
    private fun registerUser() {//Attempt to register the new User details with Firebase
        val username = username_edittext_register.text.toString() //Get the username
        val email = email_edittext_register.text.toString() //Get the User email
        val password = password_edittext_register.text.toString() //Get the User password

        if (checkUserfields(email, password, username)) {
            if (checkPasswordsMatch()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) return@addOnCompleteListener
                        Log.d("RegisterActivity", "Successfully created User with uid: ${it.result!!.user.uid}")
                        saveUserToDataBase(username)
                    }
                    .addOnFailureListener {
                        Log.d("RegisterActivity", "Failed to create User ${it.message}")
                        Toast.makeText(this, "Failed to create. ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    /**
     * Checks the user fields are populated
     * @param email User Email
     * @param password user password
     * @param username user username
     * @return Boolean, is it correctly populated or not?
     */
    private fun checkUserfields(email: String?, password: String?, username: String?):Boolean{
        if (email!!.isEmpty() || password!!.isEmpty() || username!!.isEmpty()) { //If both fields are empty, display error message to User
            Toast.makeText(this, "Please enter an email, password and username", Toast.LENGTH_SHORT).show()
            return false
        } else if(email!!.isEmpty() || password!!.isEmpty()) {
            Toast.makeText(this, "Please enter an email, password", Toast.LENGTH_SHORT).show()
            return false
        }else if(password!!.isEmpty() || username!!.isEmpty()){
            Toast.makeText(this, "Please enter a password and userName", Toast.LENGTH_SHORT).show()
            return false
        } else if(email!!.isEmpty() || username!!.isEmpty()){
            Toast.makeText(this, "Please enter a email and userName", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (email!!.isEmpty()) { //If the email is empty, display error message to User
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            return false
        } else if (password!!.isEmpty()) { //If the password is empty, display error message to User
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return false
        } else if (username!!.isEmpty()){
            Toast.makeText(this, "Please enter a userName", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * Checks both passwords are identical
     * @return Boolean
     */
    private fun checkPasswordsMatch(): Boolean{
        val passwordFirst = password_edittext_register.text.toString()
       val passwordSecond = password1_EditText_Register.text.toString()
        if (passwordFirst == passwordSecond){
            return true
        }
        Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
        return false
    }

    /**
     * Hides the users keyboard
     */
    private fun hideKeyboard(){
        try {
            val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.SHOW_FORCED)
        }
        catch(e: Exception){
            Log.d("RegisterActivity","Error closing keyboard = ${e.message}")
        }
    }

    /**Changes to IdentifyActivity
     *
     */
    private fun changeActivityToHome() {
        val intent = Intent(this, MapsActivity::class.java) //Populate intent with new activity class
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //Clear previous activities from stack
        startActivity(intent) //Change to new class
    }

    /**Saves the user to the database
     *@param Username
     */
    private fun saveUserToDataBase(Username: String) {
        Log.d("RegisterActivity", "Trying to save User") //Debug
        val role = "user"
        val uid = FirebaseAuth.getInstance().uid
            ?: "" //Elvis operator //Using the unique ID (Used to link authenticated User to the database within Firebase
        val ref = FirebaseDatabase.getInstance()
            .getReference("/users/$uid")//Using the unique ID (Used to link authenticated User to the database within Firebase) as a unique name within Firebase
        val dateTime = DateTime.getDateTime()
        val user = User(
            Username,
            role,
            dateTime
        ) //Creating new User object, to populate Firebase with

        ref.setValue(user).addOnSuccessListener {
            //Saving the User object to the Firebase database.
            Log.d("RegisterActivity", "Saved username and unique identifer to the database")
            registerUserEmail()
            changeActivityToHome() //Change actvity to homeactivity
        }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Something went wrong ${it.message}")
            }
    }

    /**Sends user an email verification
     *
     */
    private fun registerUserEmail() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("SuperTest", "Email sent.")
                }
            }
    }


}

