package com.example.testbundle.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.Transliterator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testbundle.R
import com.example.testbundle.databinding.ViewholderSizeBinding
import java.lang.System.load

class SizeAdapter(
    private val items: List<String>,
    private var selectedPosition: Int = -1,
    private val onSizeSelected: (Int) -> Unit
) : RecyclerView.Adapter<SizeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ViewholderSizeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderSizeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.sizeText.text = items[position]

        if (position == selectedPosition) {
            holder.binding.sizeLayout.setBackgroundResource(R.drawable.grey_bg_selected)
            holder.binding.sizeText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.purple))
        } else {
            holder.binding.sizeLayout.setBackgroundResource(R.drawable.grey_bg)
            holder.binding.sizeText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
        }
        holder.itemView.setOnClickListener {
            setSelectedPosition(position)
            onSizeSelected(position)
        }


    }

    fun setSelectedPosition(position: Int) {
        val prevSelected = selectedPosition
        selectedPosition = position
        notifyItemChanged(prevSelected)
        notifyItemChanged(selectedPosition)
    }


    override fun getItemCount() = items.size
}