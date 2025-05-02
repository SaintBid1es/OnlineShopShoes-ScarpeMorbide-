package com.example.testbundle.Activity.User

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.testbundle.R
import com.example.testbundle.databinding.ActivityUpdateReviewBinding

class UpdateReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdateReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }


}