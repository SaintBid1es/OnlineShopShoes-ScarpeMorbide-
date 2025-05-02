package com.example.testbundle.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.testbundle.R
import com.example.testbundle.databinding.AccountItemBinding
import com.example.testbundle.db.Item


class AccountCardAdapter(
    private val entities: List<Item>,
    private val onDelete: (id: Int) -> Unit,
    private val onEdit: (item: Item) -> Unit,
) : RecyclerView.Adapter<AccountCardAdapter.AccountHolder>() {

    class AccountHolder(
        item: View,
        private val onDelete: (id: Int) -> Unit,
        private val onEdit: (item: Item) -> Unit
    ) : RecyclerView.ViewHolder(item) {

        val binding = AccountItemBinding.bind(item)
        fun bind(item: Item) = with(binding) {
            tvInfAccount.text = itemView.context.getString(
                R.string.account_info_format,
                itemView.context.getString(R.string.account_id), item.id, // ID
                itemView.context.getString(R.string.account_email), item.email, // Email
                itemView.context.getString(R.string.account_password), "***********", // Password
                itemView.context.getString(R.string.account_speciality), item.speciality // Speciality
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.account_item, parent, false)
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