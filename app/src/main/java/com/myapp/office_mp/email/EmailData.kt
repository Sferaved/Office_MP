package com.myapp.office_mp.email

data class EmailData(
    val docNumber: String,
    var modificationDate: String,
    var comment: String,
    var subject: String,
    var orgName: String,
    var userName: String,
    var docInNum: String,
)
