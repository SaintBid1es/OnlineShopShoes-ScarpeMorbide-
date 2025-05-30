package com.example.testbundle.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.testbundle.R
import com.example.testbundle.databinding.AccountItemBinding
import com.example.testbundle.databinding.CategoryItemBinding
import com.example.testbundle.databinding.ProductUserItemBinding
import com.example.testbundle.db.Category
import com.example.testbundle.db.Item

import com.example.testbundle.db.Products

class CategoryAdapter(
    private val entities: List<Category>,
    private val onDelete: (id: Int) -> Unit,
    private val onEdit: (item: Category) -> Unit,
): RecyclerView.Adapter<CategoryAdapter.AccountHolder>()  {

    class AccountHolder(
        item: View,
        private val onDelete: (id: Int) -> Unit,
        private val onEdit: (item: Category) -> Unit
    ) : RecyclerView.ViewHolder(item) {

        val binding = CategoryItemBinding.bind(item)
        fun bind(item: Category) = with(binding) {

            tvInfCategory.text = itemView.context.getString(
                R.string.category_info_format,
                itemView.context.getString(R.string.category_name), item.namecategory
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
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