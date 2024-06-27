package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.restaurant.model.Restaurant

class   AddRestaurantActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
    val restaurantRef = database.reference.child("restaurants")
    private lateinit var restaurantName: EditText
    private lateinit var latitude: EditText
    private lateinit var longitude: EditText
    private lateinit var restaurantAddress: EditText
    private lateinit var restaurantPhoneNumber: EditText
    private lateinit var restaurantEmail: EditText
    private lateinit var restaurantWorkingFrom: EditText
    private lateinit var restaurantWorkingTo: EditText
    private lateinit var saveRestaurant: Button
    private lateinit var navView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_restaurant)

        restaurantName = findViewById(R.id.restaurantName)
        latitude = findViewById(R.id.latitude)
        longitude = findViewById(R.id.longitude)
        restaurantAddress = findViewById(R.id.restaurantAddress)
        restaurantPhoneNumber = findViewById(R.id.restaurantPhoneNumber)
        restaurantEmail = findViewById(R.id.restaurantEmail)
        restaurantWorkingFrom = findViewById(R.id.restaurantWorkingFrom)
        restaurantWorkingTo = findViewById(R.id.restaurantWorkingTo)
        saveRestaurant = findViewById(R.id.saveRestaurant)

        navView = findViewById(R.id.nav_view)

        val currentUser = FirebaseAuth.getInstance().currentUser

        saveRestaurant.setOnClickListener{
            saveRestaurant()
        }

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this@AddRestaurantActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this@AddRestaurantActivity, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@AddRestaurantActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this@AddRestaurantActivity, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }
    private fun saveRestaurant(){
        val restaurantName = restaurantName.text.toString()
        val latitude = latitude.text.toString()
        val longitude = longitude.text.toString()
        val restaurantAddress = restaurantAddress.text.toString()
        val restaurantPhoneNumber = restaurantPhoneNumber.text.toString()
        val restaurantEmail = restaurantEmail.text.toString()
        val restaurantWorkingFrom = restaurantWorkingFrom.text.toString()
        val restaurantWorkingTo = restaurantWorkingTo.text.toString()
        val user = FirebaseAuth.getInstance().currentUser
        val username = user?.email ?: ""
        val restaurantId = restaurantRef.push().key
        val restaurant = Restaurant(restaurantId.toString(), restaurantName, latitude.toDouble(), longitude.toDouble(), restaurantAddress, 0.0, 0, 0,
            restaurantWorkingFrom, restaurantWorkingTo, restaurantPhoneNumber, restaurantEmail, username)
        restaurantRef.child(restaurantId!!).setValue(restaurant)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Restaurant saved", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
                    Log.e("SaveRestaurant", "Error saving restaurant: ${task.exception}")
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
                Log.e("SaveRestaurant", "Error saving restaurant: ")
            }
    }
}