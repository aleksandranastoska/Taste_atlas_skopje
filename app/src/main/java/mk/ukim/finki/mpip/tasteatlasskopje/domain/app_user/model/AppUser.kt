package mk.ukim.finki.mpip.tasteatlasskopje.domain.app_user.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_users")
data class AppUser (val name: String,
                    val surname: String,
                    @PrimaryKey val username: String,
                    val password: String
){
    constructor(): this("", "", "", "")
}
