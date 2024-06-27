package mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.food.model.Food
import mk.ukim.finki.mpip.tasteatlasskopje.ui.activity.AddFoodReviewActivity

class FoodAdapter(private val foodList: ArrayList<Food> = ArrayList<Food>()) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foodList[position])
        Log.d("FoodAdapter", "Binding food at position $position: ${foodList[position].name}")
    }

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var foodName: TextView = view.findViewById(R.id.textViewFoodName)
        private var foodPrice: TextView = view.findViewById(R.id.textViewFoodPrice)
        private var foodImage: ImageView = view.findViewById(R.id.imageViewFood)
        var btnAddReview: Button = view.findViewById(R.id.add_food_review)

        fun bind(food: Food) {
            foodName.text = food.name
            foodPrice.text = food.price.toString()

            Glide.with(itemView.context)
                .load(food.imageUrl)
                .into(foodImage)

            btnAddReview.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, AddFoodReviewActivity::class.java)
                intent.putExtra("foodId", food.foodId)
                intent.putExtra("restaurantId", food.restaurantId)
                context.startActivity(intent)
            }
        }
    }
}