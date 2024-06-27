package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.food.model.Food
import mk.ukim.finki.mpip.tasteatlasskopje.domain.review.model.FoodReview
import java.util.Date

class AddFoodReviewActivity : AppCompatActivity() {
    val database =
        FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
    val foodReviewRef = database.reference.child("food_reviews")
    private lateinit var foodName: EditText
    private lateinit var stars: Array<ImageView>
    private lateinit var saveReview: Button
    private lateinit var navView: BottomNavigationView
    private var rating: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food_review)

        foodName = findViewById(R.id.food_name)
        stars = arrayOf(
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5)
        )
        saveReview = findViewById(R.id.btn_add_food_review)
        navView = findViewById(R.id.nav_view)

        val foodIdPassed = intent.getStringExtra("foodId")
        val restaurantIdPassed = intent.getStringExtra("restaurantId")
        val currentUser = FirebaseAuth.getInstance().currentUser

        val foodQuery: Query = database.reference.child("foods").orderByChild("foodId").equalTo(foodIdPassed)

        foodQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (foodSnapshot in dataSnapshot.children) {
                        val food: Food? = foodSnapshot.getValue(Food::class.java)
                        if (food != null) {
                            foodName.setText(food.name)
                        }
                    }
                }
            }


            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("firebase", "Error getting restaurant data", databaseError.toException())
            }
        })

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                setRating(index + 1)
            }
        }

        saveReview.setOnClickListener {
            saveReviewForFood(foodIdPassed, restaurantIdPassed)
        }

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this@AddFoodReviewActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this@AddFoodReviewActivity, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@AddFoodReviewActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this@AddFoodReviewActivity, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }
    private fun setRating(rating: Int) {
        this.rating = rating
        for (i in stars.indices) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled)
            } else {
                stars[i].setImageResource(R.drawable.ic_star_empty)
            }
        }
    }
    private fun saveReviewForFood(foodId: String?, restaurantId: String?){
        val user = FirebaseAuth.getInstance().currentUser
        val username = user?.email ?: ""


        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val foodRef = database.reference.child("foods")
        val foodQuery: Query = foodRef.orderByChild("foodId").equalTo(foodId)
        val reviewId = foodReviewRef.push().key

        val foodName = foodName.text.toString()

        val review = FoodReview(reviewId.toString(), username, foodId, grade = rating, reviewDate = Date())
        foodReviewRef.child(reviewId!!).setValue(review)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    editFood(foodId)
                    Toast.makeText(this, "Review saved", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MenuActivity::class.java)
                    intent.putExtra("restaurantId", restaurantId)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
                    Log.e("SaveReview", "Error saving review: ${task.exception}")
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
                Log.e("SaveReview", "Error saving review: ")
            }


    }

    private fun editFood(foodId: String?){
        val foodRef = database.reference.child("foods").child(foodId!!)
        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val food = dataSnapshot.getValue(Food::class.java)
                if (food != null) {
                    val oldTotalReviews = food.totalReviews ?: 0
                    val oldTotalGrade = food.totalGrade ?: 0
                    val newTotalReviews = oldTotalReviews + 1
                    val newTotalGrade = oldTotalGrade + rating
                    val newRating = newTotalGrade.toDouble() / newTotalReviews

                    val newFood = food.copy(
                        totalReviews = newTotalReviews,
                        totalGrade = newTotalGrade,
                        rating = newRating
                    )

                    Log.d("editFood", "Updating food: $newFood")

                    foodRef.setValue(newFood)
                        .addOnSuccessListener {
                            Toast.makeText(this@AddFoodReviewActivity, "Review added and food data updated successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@AddFoodReviewActivity, "Failed to update food data. Please try again.", Toast.LENGTH_SHORT).show()
                            Log.e("editFood", "Error updating food data", it)
                        }
                } else {
                    Log.e("editFood", "Food data is null for foodId: $foodId")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("editFood", "Error updating food data", databaseError.toException())
            }
        })
    }
}