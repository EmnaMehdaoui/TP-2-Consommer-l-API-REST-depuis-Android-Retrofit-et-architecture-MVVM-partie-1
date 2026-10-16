
package com.example.patients.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Consultation(
    val patient: ConsultationPatient? = null,
    val date: String = "",
    val motif: String = "",
    val traitement: String = "",
    val medecin: String = "",
    val notes: String = "",
    val statut: String = "",
    val id: String = ""
)

@Serializable
data class ConsultationPatient(
    val nom: String = "",
    val prenom: String = "",
    val id: String = ""
)
