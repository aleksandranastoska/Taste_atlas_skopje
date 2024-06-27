package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.food.model.Food
import mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters.FoodCategoryAdapter

class MenuActivity : AppCompatActivity() {
    private lateinit var recyclerViewMenu: RecyclerView
    private lateinit var foodCategoryAdapter: FoodCategoryAdapter
    private lateinit var navView: BottomNavigationView
    private val categoryMap = sortedMapOf<String, ArrayList<Food>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        navView = findViewById(R.id.nav_view)

        recyclerViewMenu = findViewById(R.id.recyclerViewMenu)
        recyclerViewMenu.layoutManager = LinearLayoutManager(this)

        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val currentUser = FirebaseAuth.getInstance().currentUser

        val restaurantId = intent.getStringExtra("restaurantId")
        loadRestaurantMenu(restaurantId!!)

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle home navigation
                    val intent = Intent(this@MenuActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this@MenuActivity, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@MenuActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this@MenuActivity, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun organizeByCategory(foodList: List<Food>) {
        foodList.forEach { food ->
            val category = food.category
            if (!categoryMap.containsKey(category)) {
                categoryMap[category] = arrayListOf()
            }
            (categoryMap[category] as MutableList<Food>).add(food)
        }
        foodCategoryAdapter = FoodCategoryAdapter(categoryMap)
        recyclerViewMenu.adapter = foodCategoryAdapter


    }

    private fun loadRestaurantMenu(restaurantId: String) {
        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val foodRef = database.reference.child("foods")

        foodRef.orderByChild("restaurantId").equalTo(restaurantId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val foodList = mutableListOf<Food>()
                    for (snapshot in dataSnapshot.children) {
                        val food = snapshot.getValue(Food::class.java)
                        food?.let { foodList.add(it) }
                    }
                    organizeByCategory(foodList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("MenuActivity", "Error passing food")
                }
            })
    }
}