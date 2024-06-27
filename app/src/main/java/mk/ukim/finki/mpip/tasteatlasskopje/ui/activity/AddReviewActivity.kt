package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.restaurant.model.Restaurant
import mk.ukim.finki.mpip.tasteatlasskopje.domain.review.model.RestaurantReview
import mk.ukim.finki.mpip.tasteatlasskopje.domain.review.model.UserReviews
import java.util.Date

class AddReviewActivity : AppCompatActivity() {
    private val CHANNEL_ID = "CHANNEL_ID"
    private val NOTIFICATION_ID = 1
    val database =
        FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
    val restaurantReviewRef = database.reference.child("restaurant_reviews")
    val userReviewRef = database.reference.child("user-restaurant-reviews")
    var restaurantIdPassed = ""
    private lateinit var restaurantName: EditText
    private lateinit var comment: EditText
    private lateinit var stars: Array<ImageView>
    private lateinit var saveReview: Button
    private lateinit var navView: BottomNavigationView
    private var rating: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)

        restaurantName = findViewById(R.id.restaurant_name)
        comment = findViewById(R.id.comment)
        stars = arrayOf(
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5)
        )
        saveReview = findViewById(R.id.btn_add_review)
        navView = findViewById(R.id.nav_view)

        restaurantIdPassed = intent.getStringExtra("restaurantId")!!

        val currentUser = FirebaseAuth.getInstance().currentUser

        val restaurantQuery: Query = database.reference.child("restaurants").orderByChild("restaurantId").equalTo(restaurantIdPassed)

        restaurantQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (restaurantSnapshot in dataSnapshot.children) {
                        val restaurant: Restaurant? = restaurantSnapshot.getValue(Restaurant::class.java)
                        if (restaurant != null) {
                            restaurantName.setText(restaurant.restaurantName)
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
            saveReviewForRestaurant(restaurantIdPassed)
        }

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this@AddReviewActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this@AddReviewActivity, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@AddReviewActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this@AddReviewActivity, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
        createNotificationChannel()
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

    private fun saveReviewForRestaurant(restaurantId: String?){
        val user = FirebaseAuth.getInstance().currentUser
        val username = user?.email ?: ""


        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val restaurantRef = database.reference.child("restaurants")
        val restaurantQuery: Query = restaurantRef.orderByChild("restaurantId").equalTo(restaurantId)
        val reviewId = restaurantReviewRef.push().key
        val userReviewId = userReviewRef.push().key

        val restaurantName = restaurantName.text.toString()
        val comment = comment.text.toString()

        val review = RestaurantReview(reviewId.toString(), comment, username, restaurantId, grade = rating, reviewDate = Date())
        restaurantReviewRef.child(reviewId!!).setValue(review)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    editRestaurant(restaurantId)
                    editUserReview(restaurantId, username)
                    Toast.makeText(this, "Review saved", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, RestaurantDetailsActivity::class.java)
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
    private fun editRestaurant(restaurantId: String?){
        val restaurantRef = database.reference.child("restaurants").child(restaurantId!!)
        restaurantRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val restaurant = dataSnapshot.getValue(Restaurant::class.java)
                if (restaurant != null) {
                    val oldTotalVisits = restaurant.totalVisits?: 0
                    val oldTotalGrade = restaurant.totalGrade?: 0
                    val newTotalReviews = oldTotalVisits +1
                    val newTotalGrade = oldTotalGrade + rating
                    val newRating = newTotalGrade.toDouble() / newTotalReviews
                    val newRestaurant = restaurant.copy(
                        totalVisits = newTotalReviews,
                        totalGrade = newTotalGrade,
                        rating = newRating
                    )
                    restaurantRef.setValue(newRestaurant)
                        .addOnSuccessListener {
                            Toast.makeText(this@AddReviewActivity, "Review added and restaurant data updated successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@AddReviewActivity, "Failed to update restaurant data. Please try again.", Toast.LENGTH_SHORT).show()
                        }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("firebase", "Error updating restaurant data", databaseError.toException())
            }
        })
    }
    private fun editUserReview(restaurantId: String?, username: String?) {
        val encodedUsername = encodeUsername(username)

        val userReviewRef = database.reference
            .child("user_restaurant_reviews")
            .child(restaurantId!!)
            .child(encodedUsername)

        userReviewRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userReviews = dataSnapshot.getValue(UserReviews::class.java)
                if (userReviews != null) {
                    val oldNumVisits = userReviews.numVisits
                    val newTotalVisits = oldNumVisits.plus(1)
                    val newUserReview = userReviews.copy(
                        numVisits = newTotalVisits,
                        hasDiscount = (newTotalVisits%10==0),
                        isDiscountUsed = false
                    )
                    userReviewRef.setValue(newUserReview)
                        .addOnSuccessListener {
                            if (newTotalVisits % 10 == 0) {
                                sendNotification("Congratulations!", "You have visited $restaurantId $newTotalVisits times! Enjoy your discount.")
                            }
                            Toast.makeText(this@AddReviewActivity, "User Review updated successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@AddReviewActivity, "Failed to update user review. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    val reviewId = userReviewRef.push().key
                    val newUserReview = UserReviews(
                        reviewId.toString(), // Assuming reviewId is generated
                        restaurantId!!,
                        encodedUsername,
                        1 , false, false
                    )

                    userReviewRef.setValue(newUserReview)
                        .addOnSuccessListener {
                            sendNotification("Congratulations!", "You have visited $restaurantId for the first time! Enjoy your discount.")
                            Toast.makeText(this@AddReviewActivity, "User Review saved successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@AddReviewActivity, "Failed to save user review. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("firebase", "Error updating user review data", databaseError.toException())
                Toast.makeText(this@AddReviewActivity, "Failed to update user review data. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun encodeUsername(username: String?): String {
        return username?.replace(".", "_") ?: ""
    }

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(this, DiscountActivity::class.java)
        intent.putExtra("restaurantId", restaurantIdPassed)
            intent.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_profile)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        } else {
            sendNotificationInternal(builder)
        }
    }





    private fun sendNotificationInternal(builder: NotificationCompat.Builder) {
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@AddReviewActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }


    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val title = "New Review"
                    val message = "A new review has been added."
                    sendNotificationInternal(
                        NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_profile)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    )
                } else {
                    Toast.makeText(
                        this,
                        "Permission denied, cannot send notification",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel name"
            val descriptionText = "Channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}