import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testbundle.R
import com.example.testbundle.databinding.ProductItemBinding
import com.example.testbundle.db.ProductsModel
import java.io.File

class ProductCardAdapter(
    private val entities: List<ProductsModel>,
    private val onDelete: (id: Int) -> Unit,
    private val onEdit: (item: ProductsModel) -> Unit,
) : RecyclerView.Adapter<ProductCardAdapter.AccountHolder>() {

    inner class AccountHolder(item: View) : RecyclerView.ViewHolder(item) {
        val binding = ProductItemBinding.bind(item)
        @SuppressLint("StringFormatMatches")
        fun bind(item: ProductsModel, context: Context) = with(binding) {
            // 1. Сначала пробуем загрузить из ресурсов
            if (item.imageId != 0) {
                ivProduct.setImageResource(item.imageId)
                Log.d("ImageLoad", "Loaded from resources: ${item.imageId}")
            }
            // 2. Пробуем загрузить из файла
            else if (!item.imageUri.isNullOrEmpty()) {
                try {
                    Log.d("ImageLoad", "Trying to load from URI: ${item.imageUri}")

                    // Получаем только имя файла из URI (удаляем путь если есть)
                    val fileName = item.imageUri.substringAfterLast("/")
                    val imagesDir = File(context.filesDir, "product_images")
                    val imageFile = File(imagesDir, fileName)

                    Log.d("ImageLoad", "Full path: ${imageFile.absolutePath}")
                    Log.d("ImageLoad", "File exists: ${imageFile.exists()}")
                    Log.d("ImageLoad", "File size: ${imageFile.length()} bytes")

                    if (imageFile.exists() && imageFile.length() > 0) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            imageFile
                        )
                        Log.d("ImageLoad", "FileProvider URI: $uri")

                        Glide.with(context)
                            .load(uri)
                            .placeholder(R.drawable.apple)
                            .error(R.drawable.arrow_ic) // Добавляем изображение для ошибок
                            .into(ivProduct)
                    } else {
                        Log.w("ImageLoad", "File not found or empty")
                        ivProduct.setImageResource(R.drawable.image_ic)
                    }
                } catch (e: Exception) {
                    Log.e("ImageLoad", "Error loading image", e)
                    ivProduct.setImageResource(R.drawable.star_ic)
                }
            }
            // 3. Если изображение не задано
            else {
                Log.d("ImageLoad", "No image specified")
                ivProduct.setImageResource(R.drawable.avatarmen)
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