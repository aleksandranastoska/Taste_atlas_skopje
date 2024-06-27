package mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.food.model.Food
import mk.ukim.finki.mpip.tasteatlasskopje.domain.restaurant.model.Restaurant

class BestDealsAdapter(private val foodList: ArrayList<Food> = ArrayList<Food>()) :
    RecyclerView.Adapter<BestDealsAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_best_deal, parent, false)
        return FoodViewHolder(view)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foodList[position])
        Log.d("BestDealsAdapter", "Binding food at position $position: ${foodList[position].name}")
    }

    class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var foodName: TextView = view.findViewById(R.id.textViewFoodName)
        private var foodPrice: TextView = view.findViewById(R.id.textViewFoodPrice)
        private var restaurantName: TextView = view.findViewById(R.id.textViewRestaurantName)
        private var foodImage: ImageView = view.findViewById(R.id.imageViewFood)

        val database = FirebaseDatabase.getInstance("https://taste-atlas-skopje-default-rtdb.europe-west1.firebasedatabase.app")


        fun bind(food: Food) {
            val restaurantRef = database.reference.child("restaurants").child(food.restaurantId!!)
            restaurantRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val restaurant = dataSnapshot.getValue(Restaurant::class.java)
                    restaurant?.let {
                        restaurantName.text = it.restaurantName
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("BestDealsAdapter", "Failed to read restaurant details", databaseError.toException())
                }
            })
            foodName.text = food.name
            foodPrice.text = food.price?.toString() ?: "N/A"

            Glide.with(itemView.context)
                .load(food.imageUrl)
                .into(foodImage)
        }
    }
}