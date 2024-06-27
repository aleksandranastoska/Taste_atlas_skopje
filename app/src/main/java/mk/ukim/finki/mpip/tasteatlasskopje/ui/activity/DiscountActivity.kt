package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.review.model.UserReviews

class DiscountActivity : AppCompatActivity() {
    val database =
        FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
    private lateinit var navView: BottomNavigationView
    private lateinit var useDiscount: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discount)

        val messageTextView: TextView = findViewById(R.id.discount_message)
        val qrCodeImageView: ImageView = findViewById(R.id.qr_code_image)

        navView = findViewById(R.id.nav_view)
        useDiscount = findViewById(R.id.useDiscount)
        val restaurantId = intent.getStringExtra("restaurantId")
        val currentUser = FirebaseAuth.getInstance().currentUser

        val discountMessage = "You are eligible for a discount!"
        messageTextView.text = discountMessage

        database.reference.child("user_restaurant_reviews").child(restaurantId!!).child(encodeUsername(currentUser?.email))
            .get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val res = dataSnapshot.getValue(UserReviews::class.java)
                    if (res != null) {
                        if (res.hasDiscount)
                            useDiscount.visibility = View.VISIBLE

                    } else {
                        useDiscount.visibility = View.GONE
                    }
                }


            }

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this@DiscountActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this@DiscountActivity, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@DiscountActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this@DiscountActivity, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
        useDiscount.setOnClickListener {
            userDiscount(restaurantId)
        }

        val qrCodeData = "Your discount code or details"
        generateQRCode(qrCodeData, qrCodeImageView)
    }

    private fun generateQRCode(text: String, imageView: ImageView) {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            imageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
    private fun encodeUsername(username: String?): String {
        return username?.replace(".", "_") ?: ""
    }
    private fun userDiscount(restaurantId: String?) {
        val user = FirebaseAuth.getInstance().currentUser
        val username = user?.email ?: ""

        val encodedUsername = encodeUsername(username)
        val userReviewRef = database.reference
            .child("user_restaurant_reviews")
            .child(restaurantId!!)
            .child(encodedUsername)

        userReviewRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userReviews = dataSnapshot.getValue(UserReviews::class.java)
                if (userReviews != null) {
                    val newUserReview = userReviews.copy(
                        isDiscountUsed = true,
                        hasDiscount = false
                    )
                    userReviewRef.setValue(newUserReview)
                        .addOnSuccessListener {
                            Toast.makeText(this@DiscountActivity, "Discount used successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@DiscountActivity, "Failed to use discount. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("firebase", "Error updating user review data", databaseError.toException())
            }
        })
    }
}