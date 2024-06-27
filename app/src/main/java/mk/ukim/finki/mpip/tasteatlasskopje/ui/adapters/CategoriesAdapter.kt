package mk.ukim.finki.mpip.tasteatlasskopje.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CategoriesAdapter(context: Context, private val categories: List<String>) : ArrayAdapter<String>(context, 0, categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false)
        }
        val textView = view as TextView
        textView.text = categories[position]
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        }
        val textView = view as TextView
        textView.text = categories[position]
        return view
    }
}