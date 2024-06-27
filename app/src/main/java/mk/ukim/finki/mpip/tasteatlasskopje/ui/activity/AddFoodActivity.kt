package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.food.model.Food
import mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters.CategoriesAdapter
import java.util.UUID


class AddFoodActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
    val storage = FirebaseStorage.getInstance("gs://taste-atlas-skopje.appspot.com")
    val categoryRef = database.reference.child("categories")
    val foodRef = database.reference.child("foods")
    private lateinit var foodName: EditText
    private lateinit var foodPrice: EditText
    private lateinit var saveFood: Button
    private lateinit var selectPhoto: Button
    private lateinit var takePhoto: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var foodImageView: ImageView
    private lateinit var navView: BottomNavigationView
    private val IMAGE_PICK_CODE = 1000
    private var selectedImageUri: Uri? = null
    private var takenImageUri: Uri? = null
    private val IMAGE_CAPTURE_CODE = 1001
    private var photoUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)


        foodName = findViewById(R.id.foodName)
        foodPrice = findViewById(R.id.foodPrice)
        saveFood = findViewById(R.id.saveFood)
        selectPhoto = findViewById(R.id.selectPhoto)
        takePhoto = findViewById(R.id.takePhoto)
        foodImageView = findViewById(R.id.foodImage)

        categorySpinner = findViewById(R.id.categorySpinner)
        navView = findViewById(R.id.nav_view)

        val currentUser = FirebaseAuth.getInstance().currentUser

        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val categories = ArrayList<String>()
                for (snapshot in dataSnapshot.children) {
                    val category = snapshot.getValue(String::class.java)
                    category?.let {
                        categories.add(it)
                    }
                }
                val adapter = CategoriesAdapter(this@AddFoodActivity, categories)
                val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
                categorySpinner.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("AddFoodActivity", "Error adding food")
            }
        })

        saveFood.setOnClickListener{
            saveFood()
        }

        selectPhoto.setOnClickListener {
            selectImageFromGallery()
        }
        takePhoto.setOnClickListener {
            openCamera()
        }
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this@AddFoodActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this@AddFoodActivity, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@AddFoodActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this@AddFoodActivity, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

    }

    private fun saveFood() {
        val foodName = foodName.text.toString()
        val foodPrice = foodPrice.text.toString()
        val foodCategory = categorySpinner.selectedItem.toString()
        val foodId = foodRef.push().key
        val restaurantId = intent.getStringExtra("restaurantId")

        val uploadUri = selectedImageUri ?: takenImageUri

        if (uploadUri  != null) {
            val food = Food(
                foodId.toString(),
                foodName,
                0,
                0,
                foodPrice.toDouble(),
                restaurantId,
                foodCategory,
                uploadUri .toString(),
                0, 0.0
            )
            val imageRef = storage.reference.child("images/${UUID.randomUUID()}")
            imageRef.putFile(uploadUri )
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        foodRef.child(foodId!!).setValue(food)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Food saved", Toast.LENGTH_SHORT).show()
                                    Log.e("ADDFOODWITHPHOTO", "REACHED ADDONCOMPLETELISTENER")
                                    val intent = Intent(this, RestaurantDetailsActivity::class.java)
                                    intent.putExtra("restaurantId", restaurantId)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
                                    Log.e("SaveFood", "Error saving food: ${task.exception}")
                                    Log.e("ADDFOODWITHPHOTO", "REACHED ADDONCOMPLETELISTENER_UNSUCCESSFULTASK")
                                }
                            }.addOnFailureListener {
                                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
                                Log.e("FOODWITHPHOTO", "Error saving food: ")
                            }
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
        else {

            val food = Food(
                foodId.toString(),
                foodName,
                0,
                0,
                foodPrice.toDouble(),
                restaurantId,
                foodCategory,
                "", 0, 0.0
            )
            foodRef.child(foodId!!).setValue(food)
                .addOnCompleteListener { task->
                    if (task.isSuccessful){
                        Toast.makeText(this, "Food saved without photo", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, RestaurantDetailsActivity::class.java)
                        Log.e("ADDFOODWITHOUTPHOTO", "REACHED ADDONCOMPLETELISTENER")
                        intent.putExtra("restaurantId", restaurantId)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
                        Log.e("SaveFood", "Error saving food: ${task.exception}")
                        Log.e("ADDFOODWITHOUTPHOTO", "REACHED ADDONCOMPLETELISTENER_UNSUCCESSFULTASK")
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
                    Log.e("SaveFood", "Error saving food: ")
                    Log.e("FOODWITHOUTPHOTO", "Error saving food: ")
                }
        }
    }
    private fun selectImageFromGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                IMAGE_PICK_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
    }
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_MEDIA_IMAGES), IMAGE_CAPTURE_CODE)
        } else {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "New Picture")
                put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            }
            photoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }
            startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            IMAGE_PICK_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImageFromGallery()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            IMAGE_CAPTURE_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            foodImageView.setImageURI(selectedImageUri)
        }
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            foodImageView.setImageURI(photoUri)
            takenImageUri = photoUri
        }
    }
}