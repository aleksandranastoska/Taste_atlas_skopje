package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.food.model.Food
import mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters.BestDealsAdapter
import mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters.SortingAdapter

class BestDealsActivity : AppCompatActivity() {
    private lateinit var adapter: BestDealsAdapter
    private lateinit var navView: BottomNavigationView
    private lateinit var filteringSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private var foodList = ArrayList<Food>()

    private var currentSortingOption = "Lowest Price"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_best_deals)

        recyclerView = findViewById(R.id.foods)
        filteringSpinner = findViewById(R.id.filteringSpinner)
        navView = findViewById(R.id.nav_view)

        adapter = BestDealsAdapter(foodList)
        recyclerView.layoutManager = LinearLayoutManager(this@BestDealsActivity)
        recyclerView.adapter = adapter

        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val foodRef = database.reference.child("foods")
        val filteringRef = database.reference.child("filteringTypes")

        loadFoodData(foodRef)

        filteringRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sortingTypes = ArrayList<String>()
                for (snapshot in dataSnapshot.children) {
                    val sortingType = snapshot.getValue(String::class.java)
                    sortingType?.let {
                        sortingTypes.add(sortingType)
                    }
                }

                val adapter = SortingAdapter(this@BestDealsActivity, sortingTypes)
                filteringSpinner.adapter = adapter

                filteringSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        currentSortingOption = sortingTypes[position]
                        Log.d("BestDealsActivity", "Selected filtering option: $currentSortingOption")
                        sortFoodList()
                        this@BestDealsActivity.adapter.notifyDataSetChanged()
                        Log.d("BestDealsActivity", "Adapter notified of data change")
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("BestDealsActivity", "Error displaying best deals")
            }
        })

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }
                R.id.navigation_best_deals -> true
                R.id.navigation_profile -> {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFoodData(foodRef: DatabaseReference) {
        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val food = snapshot.getValue(Food::class.java)
                    food?.let {
                        foodList.add(it)
                        Log.d("BestDealsActivity", "Food item added: ${it.name}")
                    }
                }
                Log.d("BestDealsActivity", "Number of foods fetched: ${foodList.size}")
                sortFoodList()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("BestDealsActivity", "Error fetching data: ${databaseError.message}")
            }
        })
    }

    private fun sortFoodList() {
        Log.d("BestDealsActivity", "Sorting by: $currentSortingOption")
        when (currentSortingOption) {
            "Highest Rated" -> {
                foodList.sortWith(compareByDescending<Food> { it.rating }.thenByDescending { it.totalReviews }.thenBy { it.name })
            }
            "Lowest Price" -> {
                foodList.sortWith(compareBy<Food> { it.price }.thenBy { it.totalReviews }.thenBy { it.name })
            }
        }
        Log.d("BestDealsActivity", "Foods sorted: ${foodList.map { it.name }}")
    }
}
