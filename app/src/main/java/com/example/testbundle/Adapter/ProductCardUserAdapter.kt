package com.example.testbundle.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.testbundle.R
import com.example.testbundle.databinding.ProductUserItemBinding
import com.example.testbundle.db.ProductsModel

class ProductCardUserAdapter(
    private var entities: List<ProductsModel>,
    private val onItemClick: (ProductsModel) -> Unit,
    private val onFavoriteClick: (Int) -> Unit
) : RecyclerView.Adapter<ProductCardUserAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ProductUserItemBinding.bind(itemView)

        @SuppressLint("SetTextI18n")
        fun bind(item: ProductsModel) {
            with(binding) {
                pic.setImageResource(item.imageId)
                priceTxt.text = "${itemView.context.getString(R.string.valuta)} ${item.cost}"
                titleTxt.text = item.name

                imgFavorite.setImageResource(
                    if (item.isFavorite) R.drawable.ic_fav else R.drawable.ic_fav1
                )

                imgFavorite.setOnClickListener {
                    onFavoriteClick(item.id ?: -1)
                }

                pic.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    fun updateList(newList: List<ProductsModel>) {
        val diffCallback = ProductDiffCallback(entities, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        entities = newList
        diffResult.dispatchUpdatesTo(this)
    }

    private class ProductDiffCallback(
        private val oldList: List<ProductsModel>,
        private val newList: List<ProductsModel>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean =
            oldList[oldPos].id == newList[newPos].id

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean =
            oldList[oldPos] == newList[newPos]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_user_item, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(entities[position])
    }

    override fun getItemCount(): Int = entities.size
}