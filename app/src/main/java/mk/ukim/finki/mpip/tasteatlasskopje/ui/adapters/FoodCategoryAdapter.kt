package mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mk.ukim.finki.mpip.tasteatlasskopje.databinding.ItemCategoryBinding
import mk.ukim.finki.mpip.tasteatlasskopje.domain.food.model.Food
import java.util.SortedMap

class FoodCategoryAdapter(private val categoryMap: SortedMap<String, ArrayList<Food>>) :
    RecyclerView.Adapter<FoodCategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryMap.keys.elementAt(position)
        val foods = categoryMap[category]!!
        holder.binding.textViewCategoryName.text = category
        val foodAdapter = FoodAdapter(foods)
        holder.binding.recyclerViewFoods.layoutManager = LinearLayoutManager(holder.binding.root.context)
        holder.binding.recyclerViewFoods.adapter = foodAdapter
    }

    override fun getItemCount(): Int {
        return categoryMap.size
    }
}