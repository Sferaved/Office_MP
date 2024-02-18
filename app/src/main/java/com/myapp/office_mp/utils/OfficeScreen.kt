package com.myapp.office_mp.utils

import androidx.annotation.StringRes
import com.myapp.office_mp.R

enum class OfficeScreen (@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Main(title = R.string.app_name),
    AboutAuthor(title = R.string.author)
}