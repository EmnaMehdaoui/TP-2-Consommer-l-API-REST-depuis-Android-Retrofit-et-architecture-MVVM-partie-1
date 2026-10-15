
package com.example.patients.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.patients.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PatientViewModel(
    private val repository: PatientRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<PatientsUiState>(PatientsUiState.Loading)

    val uiState: StateFlow<PatientsUiState> = _uiState.asStateFlow()

    fun loadPatients() {
        viewModelScope.launch {
            _uiState.value = PatientsUiState.Loading

            repository.getPatients()
                .onSuccess { patients ->
                    _uiState.value = PatientsUiState.Success(patients)
                }
                .onFailure { error ->
                    _uiState.value = PatientsUiState.Error(
                        error.message ?: "Une erreur est survenue"
                    )
                }
        }
    }
}
