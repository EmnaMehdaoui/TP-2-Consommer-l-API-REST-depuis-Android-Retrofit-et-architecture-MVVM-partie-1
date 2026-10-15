package com.example.patients.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val message: String = "Une erreur est survenue",
    val erreurs: List<String> = emptyList()
)