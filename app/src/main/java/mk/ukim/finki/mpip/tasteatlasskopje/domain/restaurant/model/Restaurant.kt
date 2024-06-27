package mk.ukim.finki.mpip.tasteatlasskopje.domain.restaurant.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Restaurant(@PrimaryKey val restaurantId: String,
                      val restaurantName: String?,
                      val latitude: Double?,
                      val longitude: Double?,
                      val address: String?,
                      val rating: Double?,
                      val totalVisits: Int?,
                      val totalGrade: Int?,
                      val workingFrom: String?,
                      val workingTo: String?,
                      val phoneNumber: String?,
                      val email: String?,
                      val owner: String
)
{
  constructor() : this("", null, null, null, null, null, null, null,null, null, null, null, "")
}