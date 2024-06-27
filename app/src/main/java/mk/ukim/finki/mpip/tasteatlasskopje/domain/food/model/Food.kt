package mk.ukim.finki.mpip.tasteatlasskopje.domain.food.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Food (@PrimaryKey val foodId: String,
                val name: String?,
                val totalReviews: Int?,
                val sumReviews: Int?,
                 val price: Double?,
                val restaurantId: String?,
                val category: String?,
                 val imageUrl: String,
    val totalGrade: Int?,
                 val rating: Double?
){
    constructor(): this("", null, null, null, null, null, null, "", null, null)
}