
package com.example.patients.ui.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest
import com.example.patients.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PatientViewModel(
    private val repository: PatientRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<PatientsUiState>(PatientsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(
        extraBufferCapacity = 1
    )
    val messages = _messages.asSharedFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus = _selectedStatus.asStateFlow()

    fun setStatusFilter(statut: String?) {
        if (_selectedStatus.value == statut) return
        _selectedStatus.value = statut
        loadPatients()
    }

    fun loadPatients() {
        viewModelScope.launch {
            _uiState.value = PatientsUiState.Loading

            repository.getPatients(_selectedStatus.value)
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

    fun addPatient(request: PatientRequest) {
        viewModelScope.launch {
            repository.createPatient(request)
                .onSuccess { patient ->
                    if (
                        _selectedStatus.value == null ||
                        _selectedStatus.value == patient.statut
                    ) {
                        updateList { patients -> patients + patient }
                    } else {
                        loadPatients()
                    }
                    _messages.emit("Patient ajouté avec succès")
                }
                .onFailure { error ->
                    _messages.emit(
                        error.message ?: "Erreur lors de l'ajout"
                    )
                }
        }
    }

    fun updatePatient(id: String, request: PatientRequest) {
        viewModelScope.launch {
            repository.updatePatient(id, request)
                .onSuccess { patient ->
                    if (
                        _selectedStatus.value == null ||
                        _selectedStatus.value == patient.statut
                    ) {
                        updateList { patients ->
                            patients.map {
                                if (it.id == id) patient else it
                            }
                        }
                    } else {
                        updateList { patients ->
                            patients.filterNot { it.id == id }
                        }
                    }
                    _messages.emit("Patient modifié avec succès")
                }
                .onFailure { error ->
                    _messages.emit(
                        error.message ?: "Erreur lors de la modification"
                    )
                }
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repository.deletePatient(patient.id)
                .onSuccess {
                    updateList { patients ->
                        patients.filterNot { it.id == patient.id }
                    }
                    _messages.emit("Patient supprimé avec succès")
                }
                .onFailure { error ->
                    _messages.emit(
                        error.message ?: "Erreur lors de la suppression"
                    )
                }
        }
    }

    private fun updateList(
        transform: (List<Patient>) -> List<Patient>
    ) {
        _uiState.update { state ->
            if (state is PatientsUiState.Success) {
                state.copy(patients = transform(state.patients))
            } else {
                state
            }
        }
    }
}
