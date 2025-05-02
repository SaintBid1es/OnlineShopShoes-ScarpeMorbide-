package com.example.testbundle.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.testbundle.R
import com.example.testbundle.databinding.BrandItemBinding
import com.example.testbundle.databinding.CategoryItemBinding
import com.example.testbundle.db.Brand
import com.example.testbundle.db.Category

class BrandAdapter (
    private val entities: List<Brand>,
    private val onDelete: (id: Int) -> Unit,
    private val onEdit: (item: Brand) -> Unit,
):RecyclerView.Adapter<BrandAdapter.AccountHolder>() {

    class AccountHolder(
        item: View,
        private val onDelete: (id: Int) -> Unit,
        private val onEdit: (item: Brand) -> Unit
    ) : RecyclerView.ViewHolder(item) {

        val binding = BrandItemBinding.bind(item)
        fun bind(item: Brand) = with(binding) {

            tvInfBrand.text = itemView.context.getString(
                R.string.brand_info_format,
                itemView.context.getString(R.string.brand_name), item.name
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.brand_item, parent, false)
        return AccountHolder(
            view, onDelete = onDelete,
            onEdit = onEdit
        )
    }
    override fun getItemCount(): Int {
        return entities.size
    }
    override fun onBindViewHolder(holder: AccountHolder, position: Int) {
        holder.bind(entities[position])
    }

}