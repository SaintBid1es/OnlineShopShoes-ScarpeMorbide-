package com.example.testbundle.Activity.User

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.Admin.ListEmployeeActivity
import com.example.testbundle.Activity.Admin.ListProductAdminActivity
import com.example.testbundle.Activity.DataStoreRepo
import com.example.testbundle.Activity.dataStore
import com.example.testbundle.Adapter.BasketAdapter
import com.example.testbundle.BasketViewModel
import com.example.testbundle.OrderItemViewModel
import com.example.testbundle.OrderViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityBasketBinding
import com.example.testbundle.db.BasketModel
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Order
import com.example.testbundle.db.OrderItem
import com.example.testbundle.db.Products
import com.example.testbundle.db.ProductsModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BasketActivity : BaseActivity() {
    lateinit var binding: ActivityBasketBinding
    lateinit var prefs: DataStore<androidx.datastore.preferences.core.Preferences>
    private var currentUserId: Int? = -1

    val viewModel: BasketViewModel by viewModels()
    private val viewModelOrder: OrderViewModel by viewModels()
    private val viewModelOrderItem: OrderItemViewModel by viewModels()



    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBasketBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        prefs = applicationContext.dataStore
        binding.rcViewBasket.layoutManager = LinearLayoutManager(this)

        /**
         * Навигационное меню
         */
        binding.imgFavorite.setOnClickListener {
            startActivity(Intent(this@BasketActivity, FavoriteActivity::class.java))
        }

        binding.imgProfile.setOnClickListener {
            startActivity(Intent(this@BasketActivity, ProfileActivity::class.java))
        }
        binding.imgMain.setOnClickListener {
            startActivity(Intent(this@BasketActivity, ListProductActivity::class.java))
        }
        binding.imgClients.setOnClickListener {
            startActivity(Intent(this@BasketActivity, ListEmployeeActivity::class.java))
        }
        binding.imgProduct.setOnClickListener {
            startActivity(Intent(this@BasketActivity, ListProductAdminActivity::class.java))
        }
        binding.imgOrderHistory.setOnClickListener {
            startActivity(Intent(this@BasketActivity, OrderHistoryActivity::class.java))
        }

        /**
         * Проверка роли
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                prefs.data.collect {
                    CheckRole(it[ProfileActivity.EMAIL_KEY], it[ProfileActivity.PASSWORD_KEY])
                }
            }
        }
        /**
         * Поиск id пользователя
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                prefs.data.collect {
                    currentUserId = it[DataStoreRepo.USER_ID_KEY]
                }
            }
        }
        /**
         * Вывод списка корзины
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.stateBasket.collect {
                    onUpdateView(it)
                }
            }
        }
    }

    /**
     * Функция проверки роли
     */
    private fun CheckRole(email: String?, password: String?) {
        val db = MainDb.getDb(this)
        db.getDao().getAllItems().asLiveData().observe(this) { list ->
            val user = list.find { it.email == email && it.password == password }
            user?.let {
                if (it.speciality == "Администратор" || it.speciality == "Administrator" )  {
                    binding.layoutUsers.isVisible = true
                    binding.layoutProduct.isVisible = true
                }
            }
        }
    }

    /**
     * Вывод списка корзины
     */
    private fun onUpdateView(entities: List<BasketModel>) {
        val totalPrice = viewModel.calculateTotalPrice(entities)
        binding.tvTotalPrice.text = "${getString(R.string.total_price)}${String.format("%.2f", totalPrice)}"
        binding.apply {
            val adapter = BasketAdapter.BasketAdapter(entities, onDelete = { item ->
                item?.let { item.id?.let { it1 -> viewModel.deleteItemByProductId(it1, it.size) } }
            }, Perexod = {
                intent =
                    Intent(this@BasketActivity, DetailProductActivity::class.java).apply {
                        putExtra("product_id", it.id)
                        putExtra("size_id", it.size)
                    }
                startActivity(intent)
            }, PlusCount = {id->
                viewModel.increaseQuantity(id)
            }, MinusCount = { id->
                viewModel.decreaseQuantity(id)
            })
            rcViewBasket.adapter = adapter
            rcViewBasket.layoutManager = GridLayoutManager(this@BasketActivity, 2)

            /**
             * Кнопка оформления заказа
             */
            binding.btnPlaceOrder.setOnClickListener {
                val isNumber = "[0123456789]".toRegex()
                val isText = Regex("[а-яА-ЯёЁa-zA-Z]")
                if (entities.isEmpty()) {
                    Toast.makeText(
                        this@BasketActivity,
                        getString(R.string.there_are_no_products_in_the_cart),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val userId = currentUserId ?: run {
                        Toast.makeText(
                            this@BasketActivity,
                            getString(R.string.user_id_is_not_available),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    val dialog = AlertDialog.Builder(this@BasketActivity)
                    val dialogView = layoutInflater.inflate(R.layout.payment_dialog, null)
                    val etNumberCard = dialogView.findViewById<EditText>(R.id.etNumberCard)
                    val etmonthAndYear = dialogView.findViewById<EditText>(R.id.etMonthAndYear)
                    val cvc = dialogView.findViewById<EditText>(R.id.etCVC)
                    val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
                    val btnLeave = dialogView.findViewById<ImageButton>(R.id.btnLeave)
                    dialog.setView(dialogView)
                    dialog.setCancelable(false)


                    etmonthAndYear.inputType = InputType.TYPE_CLASS_NUMBER
                    etmonthAndYear.filters = arrayOf(InputFilter.LengthFilter(5))

                    etmonthAndYear.addTextChangedListener(object : TextWatcher {
                        private var isFormatting = false
                        private var deletingSlash = false
                        private var deletedChar = ' '

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            if (count > 0 && !isFormatting) {
                                deletingSlash = start == 2
                                deletedChar = s?.getOrNull(start) ?: ' '
                            }
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            if (isFormatting) return

                            val text = s?.toString() ?: ""


                            if (text.length == 2 && before == 0 && !text.contains("/")) {
                                isFormatting = true
                                etmonthAndYear.setText("$text/")
                                etmonthAndYear.setSelection(3)
                                isFormatting = false
                            }

                            else if (deletingSlash && deletedChar == '/') {
                                isFormatting = true
                                etmonthAndYear.setText(text.substring(0, 1))
                                etmonthAndYear.setSelection(1)
                                isFormatting = false
                            }
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    })

                    val customdialog = dialog.create()
                    customdialog.show()

                    btnConfirm.setOnClickListener {
                        val cardNumber = etNumberCard.text.toString().replace(" ", "")
                        val cvcCode = cvc.text.toString()
                        val expiryDate = etmonthAndYear.text.toString()


                        val cardValid = isCardNumberValid(cardNumber)
                        val cvcValid = isCvcValid(cvcCode)
                        val expiryValid = isExpiryDateValid(expiryDate)


                        etNumberCard.background = ContextCompat.getDrawable(this@BasketActivity,
                            if (cardValid) R.drawable.correct_background else R.drawable.error_background)

                        cvc.background = ContextCompat.getDrawable(this@BasketActivity,
                            if (cvcValid) R.drawable.correct_background else R.drawable.error_background)

                        etmonthAndYear.background = ContextCompat.getDrawable(this@BasketActivity,
                            if (expiryValid) R.drawable.correct_background else R.drawable.error_background)

                        if (!cardValid || !cvcValid || !expiryValid) {
                            showDetailedErrorToast(cardValid, cvcValid, expiryValid)
                            return@setOnClickListener
                        }


                        val orderDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val totalPrice = entities.sumOf { it.cost * it.count }
                        val order = Order(
                            client_id = userId,
                            orderDate = orderDate,
                            totalPrice = totalPrice
                        )

                        viewModelOrder.insertOrder(order) { orderId ->
                            if (orderId == null) {
                                showErrorToast(getString(R.string.failed_to_create_order))
                                return@insertOrder
                            }

                            entities.forEach { record ->
                                val orderItem = OrderItem(
                                    orderId = orderId,
                                    productId = record.id ?: -1,
                                    productName = record.name,
                                    quantity = record.count,
                                    price = record.cost,
                                    size = record.size + 6
                                )
                                viewModelOrderItem.insertOrderItem(orderItem)
                            }

                            viewModel.deleteItem()
                            adapter.notifyDataSetChanged()
                            showSuccessToast(getString(R.string.the_order_was_successfully_completed))
                            startActivity(Intent(this@BasketActivity, BasketActivity::class.java))
                        }
                    }

                    btnLeave.setOnClickListener {
                        startActivity(Intent(this@BasketActivity, BasketActivity::class.java))
                    }
                }
            }

            /**
             * Кнопка очистки корзины
             */
            binding.btnTrashBack.setOnClickListener {
                viewModel.deleteItem()
                adapter.notifyDataSetChanged()
                Toast.makeText(
                    this@BasketActivity,
                    getString(R.string.products_successfully_clearing),
                    Toast.LENGTH_SHORT
                ).show()
            }


        }

    }

    /**
     * Валидация на банковскую карту
     * @param cvcValid [Boolean] cardValid[Boolean] expiryValid[Boolean]
     */
    fun showDetailedErrorToast(cardValid: Boolean, cvcValid: Boolean, expiryValid: Boolean) {
        val errors = mutableListOf<String>()

        if (!cardValid) errors.add(getString(R.string.error_card_number))
        if (!cvcValid) errors.add(getString(R.string.error_cvc))
        if (!expiryValid) errors.add(getString(R.string.error_expiry_date))

        val message = getString(R.string.please_correct_errors) + "\n\n" +
                errors.joinToString("\n• ")

        Toast.makeText(this@BasketActivity, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Показ успешных сообщений
     */
    fun showSuccessToast(message: String) {
        val toast = Toast.makeText(this@BasketActivity, message, Toast.LENGTH_LONG)
        toast.view?.setBackgroundColor(ContextCompat.getColor(this@BasketActivity, R.color.green))
        toast.show()
    }

    /**
     * Показ неуспешных сообщений
     */
    fun showErrorToast(message: String) {
        val toast = Toast.makeText(this@BasketActivity, message, Toast.LENGTH_LONG)
        toast.view?.setBackgroundColor(ContextCompat.getColor(this@BasketActivity, R.color.red))
        toast.show()
    }
    /**
     * Валидация на номер карты
     * @param cardNumber[String]
     */
    private fun isCardNumberValid(cardNumber: String): Boolean {
        if (cardNumber.length != 16) return false
        if (!cardNumber.matches(Regex("^[0-9]+$"))) return false
        return true
    }

    /**
     * Валидация на cvc номер
     * @param cvc[Boolean]
     */
    private fun isCvcValid(cvc: String): Boolean {
        return cvc.length == 3 && cvc.matches(Regex("^[0-9]+$"))
    }

    /**
     * Валидация на дату карты
     * @param expiryDate[String]
     */
    private fun isExpiryDateValid(expiryDate: String): Boolean {

        if (expiryDate.length != 5 || !expiryDate.matches(Regex("^(0[1-9]|1[0-2])/[0-9]{2}$"))) {
            return false
        }

        val parts = expiryDate.split("/")
        val month = parts[0].toIntOrNull() ?: return false
        val year = 2000 + (parts[1].toIntOrNull() ?: return false)

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        val minYear = currentYear
        val maxYear = currentYear + 10

        return when {
            year < minYear -> false
            year > maxYear -> false
            year == minYear -> month >= currentMonth
            else -> true
        }
    }





}

