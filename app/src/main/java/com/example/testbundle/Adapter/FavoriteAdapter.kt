package com.example.testbundle.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityBasketBinding
import com.example.testbundle.databinding.BasketItemBinding
import com.example.testbundle.databinding.ProductUserItemBinding
import com.example.testbundle.db.Basket
import com.example.testbundle.db.BasketModel
import com.example.testbundle.db.Favorite
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Products
import com.example.testbundle.db.ProductsModel

class FavoriteAdapter {


    class FavoriteAdapter(
        private val entities: List<ProductsModel>,
        private val onFavoriteClick : (id : Int) -> Unit,
        private val Perexod: (item: ProductsModel) -> Unit,
    ): RecyclerView.Adapter<FavoriteAdapter.AccountHolder>() {
        private lateinit var context: Context
        inner class AccountHolder(
            item: View,
        ) : RecyclerView.ViewHolder(item) {

            val binding = ProductUserItemBinding.bind(item)

            fun bind(item: ProductsModel) = with(binding) {

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


                priceTxt.text = "${itemView.context.getString(R.string.valuta)} ${item.cost}"
                titleTxt.text = item.name
                pic.setOnClickListener {
                    Perexod(item)
                }
                if (item.isFavorite) {
                    imgFavorite.setImageResource(R.drawable.ic_fav)
                } else {
                    imgFavorite.setImageResource(R.drawable.ic_fav1)
                }

                imgFavorite.setOnClickListener {
                    onFavoriteClick(item.id ?: -1)
                }
            }



        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            this.context = recyclerView.context
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.product_user_item, parent, false)
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