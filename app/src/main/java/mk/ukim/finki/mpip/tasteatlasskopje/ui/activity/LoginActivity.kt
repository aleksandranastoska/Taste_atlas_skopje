package mk.ukim.finki.mpip.tasteatlasskopje.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import mk.ukim.finki.mpip.tasteatlasskopje.R

class LoginActivity : AppCompatActivity() {
    private lateinit var tvRedirectSignUp: TextView
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var buttonLogin: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        username = findViewById(R.id.etUsername)
        password = findViewById(R.id.etPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        tvRedirectSignUp = findViewById(R.id.tvRedirectSignUp)

        auth = FirebaseAuth.getInstance()


        buttonLogin.setOnClickListener{
            login()
        }

        tvRedirectSignUp.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }



    }

    private fun login(){
        val userName = username.text.toString()
        val pass = password.text.toString()

        auth.signInWithEmailAndPassword(userName, pass).addOnCompleteListener(this){
            if (it.isSuccessful){
                Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}