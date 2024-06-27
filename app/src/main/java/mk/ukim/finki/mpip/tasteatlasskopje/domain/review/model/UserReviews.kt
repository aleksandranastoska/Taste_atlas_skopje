package mk.ukim.finki.mpip.tasteatlasskopje.domain.review.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserReviews(@PrimaryKey val id: String,
    val restaurantId: String?,
    val userId: String,
    val numVisits: Int,
    val hasDiscount: Boolean,
    val isDiscountUsed: Boolean)    {
    constructor(): this("", "", "", 0, false, false)
}