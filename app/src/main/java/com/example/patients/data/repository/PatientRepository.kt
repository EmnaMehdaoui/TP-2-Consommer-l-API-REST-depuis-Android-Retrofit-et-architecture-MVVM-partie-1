
package com.example.patients.data.repository

import com.example.patients.data.model.ApiError
import com.example.patients.data.model.Patient
import com.example.patients.data.remote.PatientApi
import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class PatientRepository(
    private val api: PatientApi,
    private val json: Json
) {
    suspend fun getPatients(): Result<List<Patient>> =
        safeCall { api.getPatients() }

    private suspend fun <T> safeCall(
        call: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(call())
        } catch (e: HttpException) {
            Result.failure(Exception(messageFrom(e)))
        } catch (e: IOException) {
            Result.failure(
                Exception("Serveur injoignable. Vérifiez l'adresse et le réseau.")
            )
        } catch (e: SerializationException) {
            Result.failure(
                Exception("Réponse inattendue du serveur.")
            )
        }
    }

    private fun messageFrom(e: HttpException): String {
        val body = e.response()?.errorBody()?.string()

        val apiError: ApiError? = body?.let { responseBody ->
            runCatching {
                json.decodeFromString<ApiError>(responseBody)
            }.getOrNull()
        }

        return when {
            apiError == null -> "Erreur HTTP ${e.code()}"
            apiError.erreurs.isNotEmpty() ->
                apiError.erreurs.joinToString("\n")
            else -> apiError.message
        }
    }
}
