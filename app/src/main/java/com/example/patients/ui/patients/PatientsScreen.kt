
package com.example.patients.ui.patients

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(viewModel: PatientViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadPatients()
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patients") },
                actions = {
                    TextButton(
                        onClick = { viewModel.loadPatients() }
                    ) {
                        Text("Actualiser")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedPatient = null
                    showAddDialog = true
                }
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = {
                        viewModel.setStatusFilter(null)
                    },
                    label = { Text("Tous") }
                )

                FilterChip(
                    selected = selectedStatus == "Actif",
                    onClick = {
                        viewModel.setStatusFilter("Actif")
                    },
                    label = { Text("Actifs") }
                )

                FilterChip(
                    selected = selectedStatus == "Inactif",
                    onClick = {
                        viewModel.setStatusFilter("Inactif")
                    },
                    label = { Text("Inactifs") }
                )
            }

            when (val state = uiState) {
                is PatientsUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PatientsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(state.message)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadPatients() }) {
                            Text("Réessayer")
                        }
                    }
                }

                is PatientsUiState.Success -> {
                    if (state.patients.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Aucun patient trouvé")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            items(
                                items = state.patients,
                                key = { it.id }
                            ) { patient ->
                                PatientCard(
                                    patient = patient,
                                    onEdit = {
                                        selectedPatient = patient
                                        showAddDialog = true
                                    },
                                    onDelete = {
                                        viewModel.deletePatient(patient)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPatientDialog(
            patient = selectedPatient,
            onDismiss = {
                showAddDialog = false
                selectedPatient = null
            },
            onConfirm = { patient, request ->
                if (patient == null) {
                    viewModel.addPatient(request)
                } else {
                    viewModel.updatePatient(patient.id, request)
                }
                showAddDialog = false
                selectedPatient = null
            }
        )
    }
}

@Composable
private fun PatientCard(
    patient: Patient,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onEdit() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${patient.prenom} ${patient.nom}",
                style = MaterialTheme.typography.titleMedium
            )

            patient.age?.let {
                Text("Âge : $it ans")
            }

            patient.telephone?.let {
                Text("Téléphone : $it")
            }

            patient.medecin?.let {
                Text("Médecin : $it")
            }

            Text("Statut : ${patient.statut}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Modifier")
                }

                TextButton(onClick = onDelete) {
                    Text("Supprimer")
                }
            }
        }
    }
}

@Composable
private fun AddPatientDialog(
    patient: Patient?,
    onDismiss: () -> Unit,
    onConfirm: (Patient?, PatientRequest) -> Unit
) {
    var nom by rememberSaveable(patient?.id) {
        mutableStateOf(patient?.nom ?: "")
    }
    var prenom by rememberSaveable(patient?.id) {
        mutableStateOf(patient?.prenom ?: "")
    }
    var age by rememberSaveable(patient?.id) {
        mutableStateOf(patient?.age?.toString() ?: "")
    }
    var telephone by rememberSaveable(patient?.id) {
        mutableStateOf(patient?.telephone ?: "")
    }
    var medecin by rememberSaveable(patient?.id) {
        mutableStateOf(patient?.medecin ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (patient == null) "Ajouter un patient" else "Modifier le patient")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = prenom,
                    onValueChange = { prenom = it },
                    label = { Text("Prénom") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Âge") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { Text("Téléphone") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = medecin,
                    onValueChange = { medecin = it },
                    label = { Text("Médecin") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = nom.isNotBlank() && prenom.isNotBlank(),
                onClick = {
                    val request = PatientRequest(
                        nom = nom.trim(),
                        prenom = prenom.trim(),
                        age = age.toIntOrNull(),
                        telephone = telephone.ifBlank { null },
                        medecin = medecin.ifBlank { null }
                    )
                    onConfirm(patient, request)
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
