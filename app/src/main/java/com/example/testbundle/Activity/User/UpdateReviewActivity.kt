package com.example.testbundle.Activity.User

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.testbundle.API.ApiService
import com.example.testbundle.API.RetrofitClient
import com.example.testbundle.Activity.User.ProfileActivity.Companion.idAccount
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityUpdateReviewBinding
import com.example.testbundle.db.MainDb
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class UpdateReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateReviewBinding
    private val productApi = RetrofitClient.apiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdateReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val reviewId = intent.getIntExtra("review_id", -1)

        lifecycleScope.launch {
            val review = productApi.getReviewsByID(reviewId)
            binding.etHeadingReview.setText(review.heading)
            binding.etDescriptionReview.setText(review.description)

           // binding.spinnerRating.setText(review.rating)
            val ratingArray = resources.getStringArray(R.array.rating)
            val position = ratingArray.indexOf(review.rating.toString())
            if (position >= 0) {
                binding.spinnerRating.setSelection(position)
            }

        }
        binding.btnArrowBack.setOnClickListener{
            finish()
        }

    }


}