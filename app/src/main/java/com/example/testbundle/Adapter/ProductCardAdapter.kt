import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.R
import com.example.testbundle.databinding.ProductItemBinding
import com.example.testbundle.db.ProductsModel

class ProductCardAdapter(
    private val entities: List<ProductsModel>,
    private val onDelete: (id: Int) -> Unit,
    private val onEdit: (item: ProductsModel) -> Unit,
) : RecyclerView.Adapter<ProductCardAdapter.AccountHolder>() {

    inner class AccountHolder(item: View) : RecyclerView.ViewHolder(item) {
        val binding = ProductItemBinding.bind(item)

        @SuppressLint("StringFormatMatches")
        fun bind(item: ProductsModel, context: Context) = with(binding) {
            val uri = item.imageUri
            if (!uri!!.startsWith("http://")){
                val uri = "${RetrofitClient.BASE_URL}image/$uri"
                Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.avatarmen)
                    .error(R.drawable.image_ic)
                    .into(ivProduct)
            }
            if (!uri.isNullOrEmpty() && (uri.startsWith("http://") || uri.startsWith("https://"))) {
                // Загрузка по URL
                Log.d("ImageLoad", "Loading image from URL: $uri")
                Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.avatarmen)
                    .error(R.drawable.image_ic)
                    .into(ivProduct)
            } else if (item.imageId != 0) {
                // Загрузка из ресурсов
                ivProduct.setImageResource(item.imageId)
                Log.d("ImageLoad", "Loaded from resources: ${item.imageId}")
            } else {
                // Стандартная заглушка
                ivProduct.setImageResource(R.drawable.avatarmen)
                Log.d("ImageLoad", "No image specified")
            }

            tvInfProduct.text = context.getString(
                R.string.product_info_format,
                context.getString(R.string.product_id), item.id,
                context.getString(R.string.product_name), item.name,
                context.getString(R.string.product_description), item.description,
                context.getString(R.string.product_cost), item.cost
            )

            btnDeleteCard.setOnClickListener { onDelete(item.id!!) }
            btnUpdateCard.setOnClickListener { onEdit(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return AccountHolder(view)
    }

    override fun onBindViewHolder(holder: AccountHolder, position: Int) {
        holder.bind(entities[position], holder.itemView.context)
    }

    override fun getItemCount(): Int = entities.size
}
