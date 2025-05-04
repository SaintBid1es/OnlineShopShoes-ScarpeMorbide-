package com.example.testbundle.Adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.testbundle.ProductImage
import com.example.testbundle.R
import com.example.testbundle.databinding.ProductListHorizontalItemBinding

class ProductCreateAdapter(
    private val entities: List<ProductImage>,
    initialSelectedPosition: Int = RecyclerView.NO_POSITION,
    private val onProductSelected: (Int) -> Unit
) : RecyclerView.Adapter<ProductCreateAdapter.ProductHolder>() {

    private var selectedPosition = initialSelectedPosition
    private var lastSelectedPosition = RecyclerView.NO_POSITION
    private var recyclerView: RecyclerView? = null
    private lateinit var context: Context

    inner class ProductHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val binding = ProductListHorizontalItemBinding.bind(item)

        fun bind(image: ProductImage, isChecked: Boolean) {
            binding.checkbox.setOnCheckedChangeListener(null)
            loadImage(image)
            binding.checkbox.isChecked = isChecked
            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    updateSelection(adapterPosition)
                }
            }
            itemView.setOnClickListener {
                updateSelection(adapterPosition)
            }
        }

        private fun loadImage(image: ProductImage) {
            try {
                when (image) {
                    is ProductImage.DrawableImage -> {
                        binding.ivProduct.setImageResource(image.resId)
                    }
                    is ProductImage.UriImage -> {
                        Glide.with(context)
                            .load(image.uri)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(R.drawable.avatarmen)
                            .into(binding.ivProduct)
                    }
                }
            } catch (e: Exception) {
                binding.ivProduct.setImageResource(R.drawable.avatarmen)
            }
        }

        private fun updateSelection(position: Int) {
            if (position != RecyclerView.NO_POSITION && position != selectedPosition) {
                lastSelectedPosition = selectedPosition
                selectedPosition = position
                onProductSelected(position)
                // Заменяем notifySelectionChanged() на прямой вызов:
                recyclerView?.post {
                    if (lastSelectedPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(lastSelectedPosition)
                    }
                    notifyItemChanged(selectedPosition)
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        this.context = recyclerView.context
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_list_horizontal_item, parent, false)
        return ProductHolder(view)
    }

    override fun onBindViewHolder(holder: ProductHolder, position: Int) {
        holder.bind(entities[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = entities.size

    fun setSelectedPosition(position: Int) {
        if (position in 0 until entities.size && position != selectedPosition) {
            lastSelectedPosition = selectedPosition
            selectedPosition = position

            recyclerView?.post {
                if (lastSelectedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(lastSelectedPosition)
                }
                notifyItemChanged(selectedPosition)
            }
        }
    }
}