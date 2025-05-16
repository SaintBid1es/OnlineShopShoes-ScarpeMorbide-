package com.example.testbundle.Activity.User

import ReviewViewModel
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.asLiveData
import com.example.shoesonlineshop.activity.BaseActivity
import com.example.testbundle.Activity.User.DetailProductActivity
import com.example.testbundle.Activity.User.DetailProductActivity.Companion.idUser
import com.example.testbundle.Activity.User.ListProductActivity
import com.example.testbundle.CategoryViewModel
import com.example.testbundle.ProductViewModel
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityCreateReviewBinding
import com.example.testbundle.db.Category
import com.example.testbundle.db.MainDb
import com.example.testbundle.db.Reviews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateReviewActivity : BaseActivity() {
    lateinit var binding:ActivityCreateReviewBinding
    val viewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val db = MainDb.getDb(this)
        /**
         * Реализация создания категорий
         */
        binding.btnCreate.setOnClickListener {
            val header = binding.etHeadingReview.text.toString().trim()
            val description = binding.etDescriptionReview.text.toString().trim()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val rating = binding.spinnerRating.selectedItemPosition.toDouble()+1
            val product_id = intent.getIntExtra("product_id",-1)
            val review = Reviews(null,header,description,date,rating, client_id = idUser,product_id)

            viewModel.insertReviews(review)
            Toast.makeText(this@CreateReviewActivity,
                "CreateReview", Toast.LENGTH_SHORT).show()
            val i = Intent(this@CreateReviewActivity, DetailProductActivity::class.java)
            i.putExtra("product_id",product_id)
            startActivity(i)
        }
        /**
         * Кнопка назад
         */
        binding.btnArrowBack.setOnClickListener {
            val product_id = intent.getIntExtra("product_id",-1)
            val i = Intent(this@CreateReviewActivity, DetailProductActivity::class.java)
            i.putExtra("product_id",product_id)
            startActivity(i)
        }

    }


}