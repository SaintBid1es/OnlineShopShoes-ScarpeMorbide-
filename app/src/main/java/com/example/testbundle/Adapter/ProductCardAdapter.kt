package com.example.testbundle.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.testbundle.R
import com.example.testbundle.databinding.ProductItemBinding
import com.example.testbundle.db.Products
import com.example.testbundle.db.ProductsModel

class ProductCardAdapter(
    private val entities: List<ProductsModel>,
    private val onDelete: (id: Int) -> Unit,
    private val onEdit: (item: ProductsModel) -> Unit,
): RecyclerView.Adapter<ProductCardAdapter.AccountHolder>() {
    inner class AccountHolder(
        item: View
    ) : RecyclerView.ViewHolder(item) {

        val binding = ProductItemBinding.bind(item)
        @SuppressLint("StringFormatMatches")
        fun bind(item: ProductsModel) = with(binding) {
            ivProduct.setImageResource(item.imageId)
            tvInfProduct.text =  itemView.context.getString(
                R.string.product_info_format,
                itemView.context.getString(R.string.product_id), item.id,
                itemView.context.getString(R.string.product_name), item.name,
                itemView.context.getString(R.string.product_description), item.description,
                itemView.context.getString(R.string.product_cost), item.cost
            )

            btnDeleteCard.setOnClickListener {
                onDelete(item.id!!)
            }
            btnUpdateCard.setOnClickListener {
                onEdit(item)
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return AccountHolder(
            view
        )
    }

    override fun onBindViewHolder(holder: AccountHolder, position: Int) {
        holder.bind(entities[position])
    }

    override fun getItemCount(): Int {
        return entities.size
    }

}