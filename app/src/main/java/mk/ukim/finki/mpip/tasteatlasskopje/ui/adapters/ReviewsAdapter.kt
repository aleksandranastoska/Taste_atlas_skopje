package mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.review.model.RestaurantReview
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewsAdapter(private val reviews: ArrayList<RestaurantReview> = ArrayList<RestaurantReview>()) : RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReviewsAdapter.ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_item, parent, false)
        return ReviewViewHolder(view)
    }
    override fun getItemCount(): Int {
        return reviews.size
    }

    override fun onBindViewHolder(holder: ReviewsAdapter.ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    class ReviewViewHolder(view: View): RecyclerView.ViewHolder(view){
        private var username: TextView = view.findViewById(R.id.user_name)
        private var comment: TextView = view.findViewById(R.id.comment)
        private var grade: TextView = view.findViewById(R.id.grade)
        private var reviewDate: TextView = view.findViewById(R.id.review_date)

        fun bind(restaurantReview: RestaurantReview){
            comment.text = restaurantReview.comment
            grade.text  = restaurantReview.grade.toString()
            val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(restaurantReview.reviewDate)

            reviewDate.text = formattedDate
            val currentUser = FirebaseAuth.getInstance().currentUser
            username.text = currentUser?.email.toString()
        }
    }
}