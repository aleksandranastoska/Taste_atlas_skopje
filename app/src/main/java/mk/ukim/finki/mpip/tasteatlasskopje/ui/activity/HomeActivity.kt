package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.restaurant.model.Restaurant
import mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters.RestaurantsAdapter
import mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters.SortingAdapter

class HomeActivity : AppCompatActivity() {
    private lateinit var adapter: RestaurantsAdapter
    private lateinit var addRestaurantButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var navView: BottomNavigationView
    private lateinit var sortingSpinner: Spinner

    private var currentSortingOption = "highestRating"
    private var restaurantList = ArrayList<Restaurant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        addRestaurantButton = findViewById(R.id.buttonAddRestaurant)
        recyclerView = findViewById(R.id.restaurants)
        navView = findViewById(R.id.nav_view)
        sortingSpinner = findViewById(R.id.sortingSpinner)

        adapter = RestaurantsAdapter(restaurantList)
        recyclerView.layoutManager = LinearLayoutManager(this@HomeActivity)
        recyclerView.adapter = adapter

        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val restaurantRef = database.reference.child("restaurants")
        val sortingRef = database.reference.child("sortingTypes")
        val currentUser = FirebaseAuth.getInstance().currentUser

        restaurantRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val restaurant = snapshot.getValue(Restaurant::class.java)
                    restaurant?.let {
                        restaurantList.add(it)
                    }
                }

                Log.d("HomeActivity", "Number of restaurants fetched: ${restaurantList.size}")

                sortRestaurantList()
                adapter = RestaurantsAdapter(restaurantList)
                recyclerView.layoutManager = LinearLayoutManager(this@HomeActivity)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("HomeActivity", "Error passing restaurants")
            }
        })

        sortingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sortingTypes = ArrayList<String>()
                for (snapshot in dataSnapshot.children) {
                    val sortingType = snapshot.getValue(String::class.java)
                    sortingType?.let {
                        sortingTypes.add(sortingType)
                    }
                }

                val adapter = SortingAdapter(this@HomeActivity, sortingTypes)
                sortingSpinner.adapter = adapter

                sortingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        currentSortingOption = sortingTypes[position]
                        Log.d("HomeActivity", "Selected sorting option: $currentSortingOption")
                        sortRestaurantList()
                        this@HomeActivity.adapter.notifyDataSetChanged()
                        Log.d("HomeActivity", "Adapter notified of data change")
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("HomeActivity", "Error sorting restaurants")
            }
        })

        addRestaurantButton.setOnClickListener {
            val intent = Intent(this, AddRestaurantActivity::class.java)
            startActivity(intent)
        }

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle home navigation
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this@HomeActivity, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@HomeActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun sortRestaurantList() {
        Log.d("HomeActivity", "Sorting by: $currentSortingOption")
        when (currentSortingOption) {
            "Highest Rating" -> {
                restaurantList.sortWith(compareByDescending<Restaurant> { it.rating }.thenByDescending { it.totalVisits }.thenBy { it.restaurantName })
            }
            "Lowest Rating" -> {
                restaurantList.sortWith(compareBy<Restaurant> { it.rating }.thenBy { it.totalVisits }.thenBy { it.restaurantName })
            }
            "Most Visited" -> {
                restaurantList.sortWith(compareByDescending<Restaurant> { it.totalVisits }.thenByDescending { it.rating }.thenBy { it.restaurantName })
            }
            "Least Visited" -> {
                restaurantList.sortWith(compareBy<Restaurant> { it.totalVisits }.thenByDescending { it.rating }.thenBy { it.restaurantName })
                Log.d("SortingProcess", "Restaurants sorted: ${restaurantList.map { it.restaurantName }}")
            }
        }
        Log.d("HomeActivity", "Restaurants sorted: ${restaurantList.map { it.restaurantName }}")
    }
}
