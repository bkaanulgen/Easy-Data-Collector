package com.ezdatcol.easydatacollector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import pl.droidsonroids.gif.GifImageView

class ViewPagerAdapter(private var imageList: List<Int>, private var textList: List<String>) : RecyclerView.Adapter<ViewPagerAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: GifImageView = itemView.findViewById(R.id.gifTutorial)
        val itemText: TextView = itemView.findViewById(R.id.tvTutorial)

        init {
            itemImage.setOnClickListener {
                val position = adapterPosition
                Toast.makeText(itemView.context, "You clicked on item number ${position + 1}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerAdapter.Pager2ViewHolder {
        return Pager2ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.slide_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewPagerAdapter.Pager2ViewHolder, position: Int) {
        holder.itemImage.setImageResource(imageList[position])
        holder.itemText.text = textList[position]
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}