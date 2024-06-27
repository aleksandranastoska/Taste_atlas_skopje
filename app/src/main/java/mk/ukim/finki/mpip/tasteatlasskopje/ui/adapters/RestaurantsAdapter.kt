package mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mk.ukim.finki.mpip.tasteatlasskopje.R
import mk.ukim.finki.mpip.tasteatlasskopje.domain.restaurant.model.Restaurant
import mk.ukim.finki.mpip.tasteatlasskopje.ui.activity.RestaurantDetailsActivity

class RestaurantsAdapter(private val restaurants: ArrayList<Restaurant> = ArrayList<Restaurant>()) :
    RecyclerView.Adapter<RestaurantsAdapter.RestaurantViewHolder>(){



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.restaurant_item,parent,false)
        return RestaurantViewHolder(view)
    }

    override fun getItemCount(): Int {
        return restaurants.size
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.bind(restaurants[position])
        Log.d("RestaurantsAdapter", "Binding restaurant at position $position: ${restaurants[position].restaurantName}")
    }

    class RestaurantViewHolder(view: View): RecyclerView.ViewHolder(view){
        private var restaurantName: TextView = view.findViewById(R.id.restaurant_name)
        private var latitude: TextView = view.findViewById(R.id.restaurant_latitude)
        private var longitude: TextView = view.findViewById(R.id.restaurant_longitude)
        private var restaurantAddress: TextView = view.findViewById(R.id.restaurant_address)
        private var restaurantEmail: TextView = view.findViewById(R.id.restaurant_email)
        private var restaurantWorkingFrom: TextView = view.findViewById(R.id.restaurant_working_from)
        private var restaurantWorkingTo: TextView = view.findViewById(R.id.restaurant_working_to)
        private var restaurantPhoneNumber: TextView = view.findViewById(R.id.restaurant_phone_number)
        var btnViewDetails: Button = view.findViewById(R.id.btnViewDetails)

        fun bind(restaurant: Restaurant){
            restaurantName.text = restaurant.restaurantName
            latitude.text = restaurant.latitude.toString()
            longitude.text = restaurant.longitude.toString()
            restaurantAddress.text = restaurant.address
            restaurantEmail.text=restaurant.email
            restaurantWorkingFrom.text=restaurant.workingFrom
            restaurantWorkingTo.text=restaurant.workingTo
            restaurantPhoneNumber.text=restaurant.phoneNumber

            btnViewDetails.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, RestaurantDetailsActivity::class.java)
                intent.putExtra("restaurantId", restaurant.restaurantId)
                context.startActivity(intent)
            }
        }
    }
}