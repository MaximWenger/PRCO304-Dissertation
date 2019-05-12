package com.example.planty.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.planty.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        ChangePasswordActivity_Button.setOnClickListener{
            changeUserPassword()
        }

    }

    private fun changeUserPassword(){
        val newPassword1 = ChangePasswordActivity_TextView_NewPassword.text.toString()
        val newPassword2 = ChangePasswordActivity_TextView_NewPasswordAgain.text.toString()

        if (checkUserFieldsNotEmpty(newPassword1, newPassword2)) {
            if (checkUserInput(newPassword1, newPassword2)) {
                changeUserPasswordInFirebase(newPassword1)
            }
        }
    }

    private fun changeUserPasswordInFirebase(newPassword: String){
        Log.d("ChangePasswordActivity", "Trying to save new password")
        try {
            val user = FirebaseAuth.getInstance().currentUser
           // val newPassword = "SOME-SECURE-PASSWORD"

            user?.updatePassword(newPassword)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password saved!", Toast.LENGTH_SHORT).show()
                        Log.d("ChangePasswordActivity", "User password updated.")
                    }else if(task.isCanceled){
                        Log.d("ChangePasswordActivity", "Password change cancelled")
                    }
                }
        }
        catch(e: Exception){
            Log.d("ChangePasswordActivity", "Something went wrong = ${e.message}")
        }
    }

    private fun checkUserFieldsNotEmpty(newPassword1: String, newPassword2: String):Boolean{
        if (newPassword1.isEmpty() && newPassword2.isEmpty()){
            Toast.makeText(this, "Please enter your new Password in both fields", Toast.LENGTH_SHORT).show()
            return false
        }else if (newPassword1.isEmpty()){
            Toast.makeText(this, "Please enter your new Password", Toast.LENGTH_SHORT).show()
            return false
        }else if(newPassword2.isEmpty()){
            Toast.makeText(this, "Please enter your new Password Again", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun checkUserInput(newPassword1: String, newPassword2: String):Boolean{
      if (newPassword1 == newPassword2){
         if (newPassword1.length >= 6){
             return true
         }
          Toast.makeText(this, "Your Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
          return false
    }
        Toast.makeText(this, "Please enter your new Password", Toast.LENGTH_SHORT).show()
        return false
    }


}
