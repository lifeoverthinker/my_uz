package com.example.my_uz_android.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.my_uz_android.MyUZApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Tu będziesz dodawać inicjalizatory ViewModeli
    }
}

fun CreationExtras.myUZApplication(): MyUZApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyUZApplication)