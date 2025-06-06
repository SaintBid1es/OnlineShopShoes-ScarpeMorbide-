package com.example.testbundle.Adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.testbundle.R
import com.example.testbundle.databinding.ReviewItemBinding
import com.example.testbundle.db.Item
import com.example.testbundle.db.Reviews


class ReviewAdapter(
    val clientID: Int,
    private val clientNames: Map<Int?, String>,
    private val clientAvatar: Map<Int?, Uri?>,
    private val entities: List<Reviews>,
    private val onDelete: (Int) -> Unit,
    private val onEdit: (Reviews) -> Unit,
) : RecyclerView.Adapter<ReviewAdapter.AccountHolder>() {
   inner class AccountHolder(
        item: View,
        private val onDelete: (id: Int) -> Unit,
        private val onEdit: (item: Reviews) -> Unit
    ) : RecyclerView.ViewHolder(item) {
         var uri: Uri? = null
        val binding = ReviewItemBinding.bind(item)
        fun bind(item: Reviews) = with(binding) {
            val clientName = clientNames[item.clientId] ?: "Unknown Client"
            val clientAvatar = clientAvatar[item.clientId]
            tvClientName.text = clientName
            tvTextReview.text = "${item.heading}\n ${item.description}\n ${item.rating}\n ${item.reviewdate}"

            if (clientAvatar!="null".toUri()) {
                ivAvatar.setImageURI(clientAvatar)
            }else{
                ivAvatar.setImageResource(R.drawable.avatarmen)
            }


            if (clientID!=item.clientId){
                btnDeleteCard.isVisible = false
                btnUpdateCard.isVisible = false
            }
            btnDeleteCard.setOnClickListener {
                onDelete(item.id!!)
            }

            btnUpdateCard.setOnClickListener {
                onEdit(item)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
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