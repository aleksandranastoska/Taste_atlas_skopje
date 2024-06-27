package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
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

class SearchActivity : AppCompatActivity() {
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var restaurantAdapter: RestaurantsAdapter
    private val restaurantList = ArrayList<Restaurant>()
    val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
    private val restaurantRef = database.reference.child("restaurants")
    private lateinit var navView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerView)
        navView = findViewById(R.id.nav_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        restaurantAdapter = RestaurantsAdapter(restaurantList)
        recyclerView.adapter = restaurantAdapter
        val currentUser = FirebaseAuth.getInstance().currentUser

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotEmpty()) {
                    searchRestaurants(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.isNotEmpty()) {
                    searchRestaurants(newText)
                } else {
                    restaurantList.clear()
                    restaurantAdapter.notifyDataSetChanged()
                }
                return true
            }
        })

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle home navigation
                    val intent = Intent(this@SearchActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@SearchActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this@SearchActivity, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun searchRestaurants(query: String) {
        restaurantRef.orderByChild("restaurantName").startAt(query).endAt(query + "\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    restaurantList.clear()
                    for (snapshot in dataSnapshot.children) {
                        val restaurant = snapshot.getValue(Restaurant::class.java)
                        restaurant?.let { restaurantList.add(it) }
                    }
                    restaurantAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("SearchActivity", "Error with search")
                }
            })
    }
}