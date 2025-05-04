package com.example.testbundle.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.testbundle.Activity.User.DetailProductActivity
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityBasketBinding
import com.example.testbundle.databinding.BasketItemBinding
import com.example.testbundle.databinding.ProductUserItemBinding
import com.example.testbundle.db.Basket
import com.example.testbundle.db.BasketModel
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Products
import com.example.testbundle.db.ProductsModel
import kotlinx.coroutines.CoroutineScope


class BasketAdapter {
    class BasketAdapter(
        private val entities: List<BasketModel>,
        private val onDelete: (id: BasketModel) -> Unit,
        private val Perexod: (item: BasketModel) -> Unit,
        private val PlusCount:(count: Int)->Unit,
        private val MinusCount:(count:Int)->Unit

    ): RecyclerView.Adapter<BasketAdapter.AccountHolder>() {
        private lateinit var context: Context
        inner class AccountHolder(
            item: View
        ) : RecyclerView.ViewHolder(item) {

            val binding = BasketItemBinding.bind(item)

            fun bind(item: BasketModel) = with(binding) {
                var counts: Int = item.count
                val baseCost = item.cost

                if (!item.imageUri.isNullOrEmpty()) {
                    try {
                        // Create proper URI for the image file
                        val imageUri = Uri.parse("content://com.example.testbundle.fileprovider/product_images/${item.imageUri}")
                        Glide.with(context)
                            .load(imageUri)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .error(R.drawable.avatarmen)
                            .into(pic)
                    } catch (e: Exception) {
                        pic.setImageResource(R.drawable.avatarmen)
                    }
                } else {
                    pic.setImageResource(item.imageId ?: R.drawable.avatarmen)
                }

                titleTxt.text = item.name

                priceTxt.text = "${itemView.context.getString(R.string.valuta)} %.2f".format(baseCost * counts)
                tvCount.text = counts.toString()

                btnDelete.setOnClickListener {
                    onDelete(item)
                }

                pic.setOnClickListener {
                    Perexod(item)

                }

                imgBtnMin.setOnClickListener {
                    if (counts > 1) {
                        item.id?.let { it1 -> MinusCount(it1) }
                    }
                }

                imgBtnMax.setOnClickListener {
                    item.id?.let { it1 -> PlusCount(it1) }
                }
            }



        }
        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            this.context = recyclerView.context
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.basket_item, parent, false)
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

}