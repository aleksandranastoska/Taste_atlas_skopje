package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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
import mk.ukim.finki.mpip.tasteatlasskopje.domain.review.model.RestaurantReview
import mk.ukim.finki.mpip.tasteatlasskopje.domain.review.model.UserReviews
import mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters.ReviewsAdapter


class RestaurantDetailsActivity : AppCompatActivity() {

    val database =
        FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
    private lateinit var restaurantName: TextView
    private lateinit var restaurantAddress: TextView
    private lateinit var restaurantEmail: TextView
    private lateinit var restaurantPhoneNumber: TextView
    private lateinit var restaurantWorkingFrom: TextView
    private lateinit var restaurantWorkingTo: TextView
    private lateinit var btnAddFood: Button
    private lateinit var btnEditRestaurant: Button
    private lateinit var btnAddReview: Button
    private lateinit var launchMaps: Button
    private lateinit var openMenu: Button
    private lateinit var viewDiscount: Button
    private lateinit var navView: BottomNavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewsAdapter: ReviewsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_details)
        val restaurantId = intent.getStringExtra("restaurantId")
        val currentUser = FirebaseAuth.getInstance().currentUser

        restaurantName = findViewById(R.id.restaurant_name)
        restaurantAddress = findViewById(R.id.restaurant_address)
        restaurantEmail = findViewById(R.id.restaurant_email)
        restaurantPhoneNumber = findViewById(R.id.restaurant_phone_number)
        restaurantWorkingFrom = findViewById(R.id.restaurant_working_from)
        restaurantWorkingTo = findViewById(R.id.restaurant_working_to)
        btnAddFood = findViewById(R.id.btn_add_food)
        btnEditRestaurant = findViewById(R.id.btn_edit_restaurant)
        openMenu = findViewById(R.id.openMenu)
        launchMaps = findViewById(R.id.launch_google_maps)
        btnAddReview = findViewById(R.id.add_review)
        viewDiscount = findViewById(R.id.view_discount)

        recyclerView = findViewById(R.id.reviews)
    recyclerView.layoutManager= LinearLayoutManager(this)

        navView = findViewById(R.id.nav_view)

        val reviewsRef = FirebaseDatabase.getInstance()
        .getReference("reviews")
        .orderByChild("restaurantId")
        .equalTo(restaurantId)

    val userReviewRef = database.reference
        .child("user_restaurant_reviews")
        .child(restaurantId!!)
        .child(encodeUsername(currentUser?.email))


    reviewsRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val reviewsList = ArrayList<RestaurantReview>()
            for (snapshot in dataSnapshot.children) {
                val review = snapshot.getValue(RestaurantReview::class.java)
                review?.let {
                    reviewsList.add(it)
                }
            }

            Log.d("NUMBER_REVIEWS", "Number of restaurants fetched: ${reviewsList.size}")

            reviewsAdapter = ReviewsAdapter(reviewsList)
            recyclerView.layoutManager = LinearLayoutManager(this@RestaurantDetailsActivity)
            recyclerView.adapter = reviewsAdapter
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.d("RestaurantDetailsActivity", "Error fetching restaurant data")
        }
    })



        fetchRestaurantReviews(restaurantId.toString())

        val databaseReference = FirebaseDatabase.getInstance().reference.child("restaurants")
            .child(restaurantId.toString())

        var latitude = ""
        var longitude = ""
        database.reference.child("restaurants").child(restaurantId.toString()).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val res = dataSnapshot.getValue(Restaurant::class.java)

                    restaurantName.text = res?.restaurantName
                    restaurantAddress.text = res?.address
                    restaurantEmail.text = res?.email
                    restaurantPhoneNumber.text = res?.phoneNumber
                    restaurantWorkingFrom.text = res?.workingFrom
                    restaurantWorkingTo.text = res?.workingTo
                    latitude = res?.latitude.toString()
                    longitude = res?.longitude.toString()

                    if (currentUser!=null && currentUser.email == res?.owner){
                        Log.w("visibility", "Button VISIBLE")
                        btnAddFood.visibility= View.VISIBLE
                        btnEditRestaurant.visibility= View.VISIBLE

                    } else {
                        btnAddFood.visibility = View.GONE
                        btnEditRestaurant.visibility = View.GONE
                    }
                    if (currentUser!=null && currentUser.email != res?.owner){
                        btnAddReview.visibility= View.VISIBLE

                    } else {
                        btnAddReview.visibility= View.GONE
                    }

                } else {
                    Log.w("firebase", "No restaurant found for ID: $restaurantId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("firebase", "Error getting restaurant data", exception)
            }

    database.reference.child("user_restaurant_reviews").child(restaurantId!!).child(encodeUsername(currentUser?.email))
        .get()
        .addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val res = dataSnapshot.getValue(UserReviews::class.java)
                if (res != null) {
                    if (res.hasDiscount) {
                        viewDiscount.visibility = View.VISIBLE
                    }
                    else {
                        viewDiscount.visibility = View.GONE
                    }
                }

            }


        }
        btnAddFood.setOnClickListener{
            val intent = Intent(this, AddFoodActivity::class.java)
            intent.putExtra("restaurantId", restaurantId)
            startActivity(intent)
        }
        btnEditRestaurant.setOnClickListener{
            val intent = Intent(this, EditRestaurantActivity::class.java)
            intent.putExtra("restaurantId", restaurantId)
            startActivity(intent)
        }
    openMenu.setOnClickListener {
        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("restaurantId", restaurantId)
        startActivity(intent)
    }
        launchMaps.setOnClickListener{
            val uri = Uri.parse("https://maps.google.com/maps/search/$latitude,$longitude")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            Log.println(Log.INFO, "URI", uri.toString());
            Log.w("Latitude", latitude)
            Log.w("Longitude", longitude)
            startActivity(intent)
        }
        btnAddReview.setOnClickListener{
            val intent = Intent(this, AddReviewActivity::class.java)
            intent.putExtra("restaurantId", restaurantId)
            startActivity(intent)
        }
    viewDiscount.setOnClickListener {
        val intent = Intent(this, DiscountActivity::class.java)
        intent.putExtra("restaurantId", restaurantId)
        startActivity(intent)
    }
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this@RestaurantDetailsActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this@RestaurantDetailsActivity, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@RestaurantDetailsActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this@RestaurantDetailsActivity, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }
    private fun encodeUsername(username: String?): String {
        return username?.replace(".", "_") ?: ""
    }
    private fun fetchRestaurantReviews(restaurantId: String) {
        val reviewsRef = FirebaseDatabase.getInstance()
            .getReference("restaurant_reviews")
            .orderByChild("restaurantId")
            .equalTo(restaurantId)

        reviewsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val reviewsList = ArrayList<RestaurantReview>()
                for (reviewSnapshot in dataSnapshot.children) {
                    val review = reviewSnapshot.getValue(RestaurantReview::class.java)
                    review?.let {
                        reviewsList.add(it)
                    }
                }
                reviewsList.sortByDescending { it.reviewDate }

                reviewsAdapter = ReviewsAdapter(reviewsList)
                recyclerView.adapter = reviewsAdapter
            }


            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("RestaurantDetailsActivity", "Error fetching restaurant data")
            }
        })
    }
}