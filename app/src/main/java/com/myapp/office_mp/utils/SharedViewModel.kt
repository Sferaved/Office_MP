package com.myapp.office_mp.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val passwordMap = mapOf(
        "777" to "Таня",
        "321" to "Мария"
    )

    private val _currentUser = MutableLiveData<String>()
    val currentUser: LiveData<String> get() = _currentUser

    fun checkPassword(inputPassword: String) {
        val userName = passwordMap[inputPassword]
        if (userName != null) {
            _currentUser.value = userName
        }
    }
}
