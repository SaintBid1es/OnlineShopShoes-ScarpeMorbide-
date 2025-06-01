    package com.example.testbundle.Activity.Analyst

    import android.annotation.SuppressLint
    import android.content.Intent
    import android.os.Bundle
    import android.view.View
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageButton
    import android.widget.Toast
    import androidx.activity.enableEdgeToEdge
    import androidx.activity.viewModels
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import androidx.lifecycle.lifecycleScope
    import com.example.customview.extension.dpToPx
    import com.example.testbundle.API.ApiService
    import com.example.testbundle.API.RetrofitClient
    import com.example.testbundle.Activity.RegisterActivity
    import com.example.testbundle.Activity.User.ProfileActivity
    import com.example.testbundle.OrderViewModel
    import com.example.testbundle.R
    import com.example.testbundle.Repository.AuthRepository
    import com.example.testbundle.databinding.ActivityGraphicBinding
    import com.example.testbundle.db.MainDb
    import com.example.testbundle.db.SalesData
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.Runnable
    import kotlinx.coroutines.delay
    import kotlinx.coroutines.flow.count
    import kotlinx.coroutines.flow.first
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory
    import java.util.Properties
    import javax.mail.Authenticator
    import javax.mail.Message
    import javax.mail.PasswordAuthentication
    import javax.mail.Session
    import javax.mail.Transport
    import javax.mail.internet.InternetAddress
    import javax.mail.internet.MimeMessage
    import kotlin.properties.Delegates
    import kotlin.random.Random

    class GraphicActivity : AppCompatActivity() {

        private lateinit var binding: ActivityGraphicBinding
        private val viewModelOrder: OrderViewModel by viewModels()
        private val productApi = RetrofitClient.apiService
        private lateinit var authRepository: AuthRepository
        companion object {
            var countUsersObject: Int? = null
            var avgCheckUserObject: Int? = null
            var avgRatingObject: Int? = null
            var combinedStatsObject: List<Pair<String, Int>> = emptyList()
            var summTotalPriceObject: Int? = null
            var salesStatisticsObject: List<SalesData> = emptyList()
            var dailyObject: Double? = null
            var monthlyObject: Double? = null
            var yearlyObject: Double? = null
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityGraphicBinding.inflate(layoutInflater)
            setContentView(binding.root)
            authRepository = AuthRepository(applicationContext)
            binding.imgProfile.setOnClickListener {
                startActivity(Intent(this@GraphicActivity, ProfileActivity::class.java))
            }
            binding.imgGmail.setOnClickListener {
                showConfirmEmailDialog()
            }
        }

        override fun onStart() {
            super.onStart()


            // Инициализация всех графиков как невидимых
            listOf(
                binding.analyticalPieChart1,
                binding.analyticalPieChart2,
                binding.analyticalPieChart3,
                binding.analyticalPieChart4,
                binding.analyticalPieChart5,
                binding.analyticalPieChart6,
                binding.analyticalPieChart7,
                binding.analyticalPieChart8,
                binding.analyticalPieChart9
            ).forEach { chart ->
                chart.visibility = android.view.View.INVISIBLE
              // Включаем анимацию текста
            }

            lifecycleScope.launch {
                loadAllChartsSequentially()
            }
        }

        private suspend fun loadAllChartsSequentially() {
            // Загружаем графики последовательно с небольшими задержками
            delay(150)
            loadChartWithAnimation(1) {
                val token = authRepository.getAccessToken()!!
                    val countUsers = productApi.getUsers(token).count()

                countUsersObject=countUsers
                listOf(Pair(countUsers, "Всего пользователей"))
            }

            delay(150)
            loadChartWithAnimation( 2) {

                val avgCheckUser = productApi.avgTotalPrice().toString().toInt()
                avgCheckUserObject = avgCheckUser
                listOf(Pair(avgCheckUser, "Средний чек пользователя"))
            }

            delay(150)
            loadChartWithAnimation(3) {
                val avgRating = productApi.avgRating().toString().toInt()
                avgRatingObject = avgRating
                listOf(Pair(avgRating, "Средний рейтинг товаров"))
            }

            delay(150)
            loadChartWithAnimation(4) {
                val summTotalPrice = productApi.summTotalPrice().toString().toInt()
                summTotalPriceObject = summTotalPrice
                binding.analyticalPieChart4.setCirclePadding(this.dpToPx(10))

                listOf(

                    Pair(summTotalPrice, "Общая выручка")  // Основная подпись
                )
            }

            delay(150)
            loadChartWithAnimation(5) {
                val data = viewModelOrder.getProductSalesStatistics()
                salesStatisticsObject = data
                // Преобразуем SalesData в Pair<Int, String>
                data.map { salesData ->
                    Pair(salesData.quantity, "${salesData.name} (продано ${salesData.quantity} шт.)")
                }
            }

            delay(150)
            loadChartWithAnimation(6) {
                val combinedStats = viewModelOrder.getCombinedBrandCategoryStatsOptimized()
                combinedStatsObject = combinedStats
                combinedStats.map {
                    val label = when {
                        it.first.startsWith("Бренд:") -> it.first.replace("Бренд:", "Бренд:") + " (${it.second})"
                        it.first.startsWith("Категория:") -> it.first.replace("Категория:", "Категория:") + " (${it.second})"
                        else -> it.first + " (${it.second})"
                    }
                    // Меняем местами Int и String
                    Pair(it.second, label)
                }
            }

            delay(150)
            loadRevenueCharts()
        }

        private suspend fun loadChartWithAnimation(
            chartNumber: Int,
            dataProvider: suspend () -> List<Pair<Int, String>>
        ) {
            try {
                val chart = when (chartNumber) {
                    1 -> binding.analyticalPieChart1
                    2 -> binding.analyticalPieChart2
                    3 -> binding.analyticalPieChart3
                    4 -> binding.analyticalPieChart4
                    5 -> binding.analyticalPieChart5
                    6 -> binding.analyticalPieChart6.apply { setTitle("Статистика магазина") }
                    else -> throw IllegalArgumentException("Invalid chart number")
                }

                val chartData = dataProvider()
                chart.setDataChart(chartData)
                chart.visibility = android.view.View.VISIBLE
                chart.startAnimation()

                // Принудительное обновление отображения
                chart.invalidate()
                chart.requestLayout()

            } catch (e: Exception) {
                Toast.makeText(
                    this@GraphicActivity,
                    "Ошибка загрузки графика $chartNumber: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private suspend fun loadRevenueCharts() {
            try {
                val (daily, monthly, yearly) = viewModelOrder.getRevenueStatistics()
                dailyObject = daily
                monthlyObject = monthly
                yearlyObject= yearly

                withContext(Dispatchers.Main) {
                    // Выручка за день
                    binding.analyticalPieChart7.setDataChart(listOf(Pair(daily.toInt(), "Выручка за день")))
                    binding.analyticalPieChart7.visibility = View.VISIBLE
                    binding.analyticalPieChart7.startAnimation()
                    binding.analyticalPieChart7.setCircleSize(50)
                    // Выручка за месяц
                    binding.analyticalPieChart8.setDataChart(listOf(Pair(monthly.toInt(), "Выручка за месяц")))
                    binding.analyticalPieChart8.visibility = View.VISIBLE
                    binding.analyticalPieChart8.startAnimation()
                    binding.analyticalPieChart8.setCircleSize(50)
                    // Выручка за год
                    binding.analyticalPieChart9.setDataChart(listOf(Pair(yearly.toInt(), "Выручка за год")))
                    binding.analyticalPieChart9.visibility = View.VISIBLE
                    binding.analyticalPieChart9.startAnimation()
                    binding.analyticalPieChart9.setCircleSize(50)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@GraphicActivity,
                    "Ошибка загрузки данных о выручке: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        fun sendEmail(toEmail: String) {
            try {
                val props = Properties()
                props.setProperty("mail.transport.protocol", "smtp")
                props.setProperty("mail.host", "smtp.gmail.com")
                props.put("mail.smtp.auth", "true")
                props.put("mail.smtp.port", "465")
                props.put("mail.smtp.socketFactory.port", "465")
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")

                val session = Session.getDefaultInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        //от кого
                        return PasswordAuthentication("isip_m.a.vesenkov@mpt.ru", "mbmtqsqhtxxwurzn")
                    }
                })
                val message = MimeMessage(session)//от кого
                message.setFrom(InternetAddress("isip_m.a.vesenkov@mpt.ru"))//куда
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                message.subject = "This is Statistic Shop:"
                message.setText("Всего пользователей: $countUsersObject\nСредний чек пользователя: $avgCheckUserObject\nСредний рейтинг товаров: $avgRatingObject\nОбщая выручка: $summTotalPriceObject\nСравнение проданных брендов и категорий: $combinedStatsObject\nСравнение проданных товаров: $salesStatisticsObject\nВыручка за день: $dailyObject\nВыручка за месяц: $monthlyObject\nВыручка за год: $yearlyObject" )

                val transport = session.getTransport("smtp")
                transport.connect()
                Transport.send(message)
                transport.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //то что я хочу
        }
        @SuppressLint("ResourceAsColor")
        private fun showConfirmEmailDialog() {
            val dialog = AlertDialog.Builder(this@GraphicActivity)
            val dialogView = layoutInflater.inflate(R.layout.input_email_dialog, null)
            val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
            val btnLeave = dialogView.findViewById<ImageButton>(R.id.btnLeave)
            dialog.setView(dialogView)
            dialog.setCancelable(false)
            val customdialog = dialog.create()

            btnConfirm.setOnClickListener {
                val email = etEmail.text.toString()
                Thread(java.lang.Runnable {
                    sendEmail(email)
                }).start()
                customdialog.dismiss()
            }
            btnLeave.setOnClickListener {
                customdialog.dismiss()
            }
            customdialog.show()
        }

    }