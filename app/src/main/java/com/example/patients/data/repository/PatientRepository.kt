
package com.example.patients.data.repository

import com.example.patients.data.model.ApiError
import com.example.patients.data.model.Consultation
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import com.example.patients.data.remote.PatientApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.HttpException
import java.io.IOException

class PatientRepository(
    private val api: PatientApi,
    private val json: Json
) {

    suspend fun getPatients(
        statut: String? = null
    ): Result<List<Patient>> = safeCall {
        api.getPatients(statut)
    }

    suspend fun getPatient(
        id: String
    ): Result<Patient> = safeCall {
        api.getPatient(id)
    }

    suspend fun createPatient(
        request: PatientRequest
    ): Result<Patient> = safeCall {
        api.createPatient(request)
    }

    suspend fun updatePatient(
        id: String,
        request: PatientRequest
    ): Result<Patient> = safeCall {
        api.updatePatient(id, request)
    }

    suspend fun deletePatient(
        id: String
    ): Result<Unit> = safeCall {
        val response = api.deletePatient(id)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }

    suspend fun getConsultations(
        patientId: String
    ): Result<List<Consultation>> = safeCall {
        api.getConsultations(patientId)
    }

    private suspend fun <T> safeCall(
        call: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(call())
        } catch (e: HttpException) {
            Result.failure(Exception(messageFrom(e)))
        } catch (e: IOException) {
            Result.failure(
                Exception("Serveur injoignable. Vérifiez la connexion.")
            )
        } catch (e: SerializationException) {
            Result.failure(
                Exception("Réponse inattendue du serveur.")
            )
        }
    }

    private fun messageFrom(e: HttpException): String {
        val body = e.response()?.errorBody()?.string()

        if (!body.isNullOrBlank()) {
            try {
                val error = json.decodeFromString<ApiError>(body)
                return error.message
            } catch (_: Exception) {
                // Le corps d'erreur n'est pas décodable.
            }
        }

        return "Erreur HTTP ${e.code()}"
    }
}
