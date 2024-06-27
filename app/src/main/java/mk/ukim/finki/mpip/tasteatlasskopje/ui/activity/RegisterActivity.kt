package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.firebase.ktx.Firebase
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import mk.ukim.finki.mpip.tasteatlasskopje.R

class RegisterActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var buttonSignUp: Button
    lateinit var tvRedirectLogin: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.editTextTextPersonName)
        etSurname = findViewById(R.id.editTextTextPersonSurname)
        etUsername = findViewById(R.id.editTextTextPersonUsername)
        etPassword = findViewById(R.id.editTextTextPersonPassword)
        buttonSignUp = findViewById(R.id.registerButton)
        tvRedirectLogin = findViewById(R.id.tvRedirectLogin)

        auth = Firebase.auth

        buttonSignUp.setOnClickListener{
            signUpUser()
        }

        tvRedirectLogin.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signUpUser(){
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val username = etUsername.text.toString()
        val password = etPassword.text.toString()

        if (username.isBlank() || password.isBlank()){
            Toast.makeText(this, "Username and Password can not be blank", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(this){
            if (it.isSuccessful){
                Toast.makeText(this, "Successfully signed up", Toast.LENGTH_SHORT).show()
                val user = auth.currentUser
                val userId = user?.uid
                saveUserData(userId, name, surname, username)
                finish()
            } else {
                Toast.makeText(this, "Signing up failed!", Toast.LENGTH_SHORT).show()

            }
        }
    }
    private fun saveUserData(userId: String?, name: String, surname: String, username: String) {
        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")
        val usersRef = database.getReference("users")
        userId?.let {
            val userRef = usersRef.child(userId)
            userRef.child("name").setValue(name)
            userRef.child("surname").setValue(surname)
            userRef.child("username").setValue(username)
        }
    }
}