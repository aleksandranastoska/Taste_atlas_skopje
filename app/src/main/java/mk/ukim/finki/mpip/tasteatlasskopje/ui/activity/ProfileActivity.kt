package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.app_user.model.AppUser
import mk.ukim.finki.mpip.tasteatlasskopje.domain.restaurant.model.Restaurant
import mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters.RestaurantsAdapter

class ProfileActivity : AppCompatActivity() {
    private lateinit var userName: TextView
    private lateinit var userSurname: TextView
    private lateinit var email: TextView
    private lateinit var navView: BottomNavigationView
    private lateinit var adapter: RestaurantsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnEditUser: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userName = findViewById(R.id.user_name)
        userSurname = findViewById(R.id.user_surname)
        email = findViewById(R.id.user_email)
        navView = findViewById(R.id.nav_view)
        recyclerView = findViewById(R.id.restaurants)
        btnEditUser = findViewById(R.id.btneditUser)

        val emailPassed = intent.getStringExtra("user_name")

        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")

        val userQuery: Query = database.reference.child("users").orderByChild("username").equalTo(emailPassed)

        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val user: AppUser? = userSnapshot.getValue(AppUser::class.java)
                        if (user != null) {
                            userName.text = user.name
                            userSurname.text = user.surname
                            email.text = user.username
                        }
                        }
                    }
                }


            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("firebase", "Error getting user data", databaseError.toException())
            }
        })

        val restaurantRef = FirebaseDatabase.getInstance().reference.child("restaurants").orderByChild("owner").equalTo(emailPassed)

        restaurantRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val restaurantList = ArrayList<Restaurant>()
                for (snapshot in dataSnapshot.children) {
                    val restaurant = snapshot.getValue(Restaurant::class.java)
                    restaurant?.let {
                        restaurantList.add(it)
                    }
                }

                Log.d("ProfileActivity", "Number of restaurants fetched: ${restaurantList.size}")


                adapter = RestaurantsAdapter(restaurantList)
                recyclerView.layoutManager = LinearLayoutManager(this@ProfileActivity)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ProfileActivity", "Error passing profile data")
            }
        })


        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this@ProfileActivity, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@ProfileActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    true
                }

                else -> false
            }
        }

        btnEditUser.setOnClickListener {
            val intent = Intent(this, EditUserActivity::class.java)
            intent.putExtra("user_name", emailPassed)
            startActivity(intent)
        }
    }
}
