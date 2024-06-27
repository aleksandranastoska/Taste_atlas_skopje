package mk.ukim.finki.mpip.tasteatlasskopje.domain.review.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date

@Entity
data class FoodReview(@PrimaryKey val reviewId: String,
    val userId: String,
    val foodId: String?,
    val grade: Int,
    val reviewDate: Date
) {
    constructor(): this("", "", "", 0, SimpleDateFormat("yyyy-MM-dd").parse("1900-01-01"))
}