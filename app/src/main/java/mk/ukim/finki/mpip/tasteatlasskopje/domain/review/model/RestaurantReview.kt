package mk.ukim.finki.mpip.tasteatlasskopje.domain.review.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date

@Entity
data class RestaurantReview (@PrimaryKey val reviewId: String,
                        val comment: String?,
                       val userId: String,
                       val restaurantId: String?,
                       val grade: Int,
                       val reviewDate: Date
) {
    constructor(): this("", null, "", "", 0, SimpleDateFormat("yyyy-MM-dd").parse("1900-01-01"))
}