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
import mk.ukim.finki.mpip.tasteatlasskopje.domain.app_user.model.AppUser

class EditUserActivity : AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var surname: EditText
    private lateinit var email: EditText
    private lateinit var oldPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var saveDetails: Button
    private lateinit var deleteUser: Button
    private lateinit var navView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        name = findViewById(R.id.name)
        surname = findViewById(R.id.surname)
        email = findViewById(R.id.email)
        oldPassword = findViewById(R.id.oldPassword)
        newPassword = findViewById(R.id.newPassword)
        saveDetails = findViewById(R.id.saveDetails)
        deleteUser = findViewById(R.id.deleteUser)

        navView = findViewById(R.id.nav_view)

        val emailPassed = intent.getStringExtra("user_name")
        var oldPassword = ""

        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")

        val currentUser = FirebaseAuth.getInstance().currentUser

        val userQuery: Query = database.reference.child("users").orderByChild("username").equalTo(emailPassed)

        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val user: AppUser? = userSnapshot.getValue(AppUser::class.java)
                        if (user != null) {
                            name.setText(user.name)
                            surname.setText(user.surname)
                            email.setText(user.username)
                            oldPassword = user.password
                        }
                    }
                }
            }


            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("firebase", "Error getting user data", databaseError.toException())
            }
        })


        saveDetails.setOnClickListener {
                saveUserDetails(emailPassed, oldPassword)
        }

        deleteUser.setOnClickListener {
            deleteUserFromDatabase(emailPassed)
        }

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this@EditUserActivity, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this@EditUserActivity, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_best_deals -> {
                    val intent = Intent(this@EditUserActivity, BestDealsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this@EditUserActivity, ProfileActivity::class.java)
                    intent.putExtra("user_name", currentUser?.email)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }

    private fun deleteUserFromDatabase(emailPassed: String?) {
        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val userRef = database.reference.child("users")

        val userQuery: Query = userRef.orderByChild("username").equalTo(emailPassed)

        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val userKey = userSnapshot.key
                        if (userKey != null) {
                            userRef.child(userKey).removeValue()
                                .addOnSuccessListener {
                                    Log.d("firebase", "User deleted successfully")
                                    val intent = Intent(this@EditUserActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("firebase", "Error deleting user", e)
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



    private fun saveUserDetails(emailPassed: String?, oldpassword: String?) {
        val newName = name.text.toString().trim()
        val newSurname = surname.text.toString().trim()
        val newEmail = email.text.toString().trim()
        val newOldPassword = oldPassword.text.toString().trim()
        val newNewPassword = newPassword.text.toString().trim()

        val updatedUser = AppUser(newName, newSurname, newEmail, newNewPassword)

        val database =
            FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val userRef = database.reference.child("users")
        val userQuery: Query = userRef.orderByChild("username").equalTo(emailPassed)

        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val userKey = userSnapshot.key
                        if (userKey != null) {
                            userRef.child(userKey).setValue(updatedUser)
                                .addOnSuccessListener {
                                    Log.d("firebase", "User details updated successfully")
                                    Toast.makeText(
                                        this@EditUserActivity,
                                        "User details saved successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent =
                                        Intent(this@EditUserActivity, ProfileActivity::class.java)
                                    intent.putExtra("user_name", emailPassed)
                                    startActivity(intent)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("firebase", "Error updating user details", e)
                                    Toast.makeText(
                                        this@EditUserActivity,
                                        "Failed to save user details",
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
                    this@EditUserActivity,
                    "Failed to update user details",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}

