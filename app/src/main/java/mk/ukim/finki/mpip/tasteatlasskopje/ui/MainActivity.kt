package mk.ukim.finki.mpip.tasteatlasskopje.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.ui.activity.LoginActivity
import mk.ukim.finki.mpip.tasteatlasskopje.ui.activity.RegisterActivity

class MainActivity : AppCompatActivity() {
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        registerButton.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}