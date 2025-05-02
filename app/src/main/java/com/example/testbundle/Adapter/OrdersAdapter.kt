import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testbundle.R
import com.example.testbundle.db.Order
import com.example.testbundle.db.OrderItem
import com.example.testbundle.db.OrderModel
import java.util.UUID

class OrderHistoryAdapter(
    private var orders: List<OrderModel>,

) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = orders.size

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderId: TextView = itemView.findViewById(R.id.orderId)
        private val orderDate: TextView = itemView.findViewById(R.id.orderDate)
        private val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
        private val itemsList: TextView = itemView.findViewById(R.id.itemsList)

        fun bind(order: OrderModel) {
            val sizeLabel = itemView.context.getString(R.string.size)
            val valuta = itemView.context.getString(R.string.valuta)
            orderId.text = itemView.context.getString(R.string.order_id, order.id.toString().takeLast(6))
            orderDate.text = itemView.context.getString(R.string.order_date, order.orderDate)
            totalPrice.text = itemView.context.getString(R.string.order_total_price, itemView.context.getString(R.string.valuta ), order.totalPrice)


            val itemsText = order.products
                .replace("(", "$sizeLabel ")
                .replace(")", "")
                .replace("%s", valuta)
            itemsList.text = itemsText
        }

    }
}