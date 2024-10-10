package com.example.runpal.server

class GenericResponse<T>(
    var message: String = "",
    var data: T? = null
) {
}