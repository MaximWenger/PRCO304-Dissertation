package com.example.planty.Classes

import android.util.Log
import com.example.planty.Objects.UserImage
import com.google.firebase.database.DataSnapshot
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_identified.*

class FirebaseImage {

     fun getSingleImgLoc(p0: DataSnapshot, currentIdentImage: String): String?{ //returns image location from firebase
      //  var imgLoc: String? = ""
         var imgLoc: String? = ""
        p0.children.forEach {
            if (it.key.toString() == currentIdentImage) { //Compares the imageName to the Id name, to confirm the correct image details are loaded
            Log.d("FirebaseImage", "KEy =  ${it.key.toString()} and ImageName = ${currentIdentImage}")
                val currentImage = it.getValue(UserImage::class.java)
                 imgLoc = currentImage?.imageLoc
                Log.d("FirebaseImage", "imgLoc =  ${imgLoc}}")
            }
        }
         return imgLoc
    }

}