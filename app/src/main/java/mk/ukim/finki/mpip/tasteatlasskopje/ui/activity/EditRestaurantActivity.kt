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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.restaurant.model.Restaurant

class EditRestaurantActivity : AppCompatActivity() {
    private lateinit var restaurantName: EditText
    private lateinit var latitude: EditText
    private lateinit var longitude: EditText
    private lateinit var restaurantAddress: EditText
    private lateinit var restaurantPhoneNumber: EditText
    private lateinit var restaurantEmail: EditText
    private lateinit var restaurantWorkingFrom: EditText
    private lateinit var restaurantWorkingTo: EditText
    private lateinit var saveRestaurantDetails: Button
    private lateinit var deleteRestaurant: Button
    private lateinit var navView: BottomNavigationView
    private var currentRestaurant: Restaurant? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_restaurant)

        restaurantName = findViewById(R.id.restaurantName)
        latitude = findViewById(R.id.latitude)
        longitude = findViewById(R.id.longitude)
        restaurantAddress = findViewById(R.id.restaurantAddress)
        restaurantPhoneNumber = findViewById(R.id.restaurantPhoneNumber)
        restaurantEmail = findViewById(R.id.restaurantEmail)
        restaurantWorkingFrom = findViewById(R.id.restaurantWorkingFrom)
        restaurantWorkingTo = findViewById(R.id.restaurantWorkingTo)
        saveRestaurantDetails = findViewById(R.id.saveRestaurantDetails)
        deleteRestaurant = findViewById(R.id.deleteRestaurant)

        navView = findViewById(R.id.nav_view)

        val restaurantIdPassed = intent.getStringExtra("restaurantId")

        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val currentUser = FirebaseAuth.getInstance().currentUser

        val restaurantQuery: Query = database.reference.child("restaurants").orderByChild("restaurantId").equalTo(restaurantIdPassed)

        restaurantQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (restaurantSnapshot in dataSnapshot.children) {
                        val restaurant: Restaurant? = restaurantSnapshot.getValue(Restaurant::class.java)
                        if (restaurant != null) {
                            currentRestaurant = restaurant
                            restaurantName.setText(restaurant.restaurantName)
                            latitude.setText(restaurant.latitude.toString())
                            longitude.setText(restaurant.longitude.toString())
                            restaurantAddress.setText(restaurant.address)
                            restaurantPhoneNumber.setText(restaurant.phoneNumber)
                            restaurantEmail.setText(restaurant.email)
                            restaurantWorkingFrom.setText(restaurant.workingFrom)
                            restaurantWorkingTo.setText(restaurant.workingTo)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("firebase", "Error getting restaurant data", databaseError.toException())
            }
        })

        saveRestaurantDetails.setOnClickListener {
            saveRestaurantDetails(restaurantIdPassed)
        }

        deleteRestaurant.setOnClickListener {
            deleteRestaurantFromDatabase(restaurantIdPassed)
        }
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this@EditRestaurantActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this@EditRestaurantActivity, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@EditRestaurantActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this@EditRestaurantActivity, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }

    private fun deleteRestaurantFromDatabase(restaurantId: String?) {
        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val restaurantRef = database.reference.child("restaurants")

        val restaurantQuery: Query = restaurantRef.orderByChild("restaurantId").equalTo(restaurantId)

        restaurantQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (restaurantSnapshot in dataSnapshot.children) {
                        val restaurantKey = restaurantSnapshot.key
                        if (restaurantKey != null) {
                            restaurantRef.child(restaurantKey).removeValue()
                                .addOnSuccessListener {
                                    Log.d("firebase", "Restaurant deleted successfully")
                                    val intent = Intent(this@EditRestaurantActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("firebase", "Error deleting restaurant", e)
                                }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("firebase", "Error getting user data", databaseError.toException())
            }
        })
    }

    private fun saveRestaurantDetails(restaurantId: String?){
        if (currentRestaurant == null) {
            Log.e("firebase", "Restaurant data is not available")
            Toast.makeText(this, "Restaurant data is not available", Toast.LENGTH_SHORT).show()
            return
        }
        val newRestaurantName = restaurantName.text.toString().trim()
        val newLatitude = latitude.text.toString().trim()
        val newLongitude = longitude.text.toString().trim()
        val newAddress = restaurantAddress.text.toString().trim()
        val newPhoneNumber = restaurantPhoneNumber.text.toString().trim()
        val newEmail = restaurantEmail.text.toString().trim()
        val newWorkingFrom = restaurantWorkingFrom.text.toString().trim()
        val newWorkingTo = restaurantWorkingTo.text.toString().trim()
        val user = FirebaseAuth.getInstance().currentUser
        val username = user?.email ?: ""

        val updatedRestaurant = currentRestaurant!!.copy(
            restaurantName = newRestaurantName,
            latitude = newLatitude.toDouble(),
            longitude = newLongitude.toDouble(),
            address = newAddress,
            phoneNumber = newPhoneNumber,
            email = newEmail,
            workingFrom = newWorkingFrom,
            workingTo = newWorkingTo,
            owner = username
        )


        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val restaurantRef = database.reference.child("restaurants")
        val restaurantQuery: Query = restaurantRef.orderByChild("restaurantId").equalTo(restaurantId)
        restaurantQuery.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (restaurantSnapshot in snapshot.children){
                        val restaurantKey = restaurantSnapshot.key
                        if (restaurantKey!=null){
                            restaurantRef.child(restaurantKey).setValue(updatedRestaurant)
                                .addOnSuccessListener {
                                    Log.d("firebase", "Restaurant details updated succesfully")
                                    Toast.makeText(this@EditRestaurantActivity, "Restaurant updated successfully", Toast.LENGTH_SHORT).show()
                                    val intent =
                                        Intent(this@EditRestaurantActivity, RestaurantDetailsActivity::class.java)
                                    intent.putExtra("restaurantId", restaurantId)
                                    startActivity(intent)
                                }
                                .addOnFailureListener {
                                    Log.e("firebase", "Error updating restaurant details")
                                    Toast.makeText(
                                        this@EditRestaurantActivity,
                                        "Failed to save restaurant details",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("firebase", "Error getting user data", databaseError.toException())
                Toast.makeText(
                    this@EditRestaurantActivity,
                    "Failed to update user details",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

}