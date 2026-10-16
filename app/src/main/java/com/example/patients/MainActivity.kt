
package com.example.patients

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.di.AppContainer
import com.example.patients.ui.patients.PatientViewModel
import com.example.patients.ui.patients.PatientsScreen
import com.example.patients.ui.theme.PatientsTheme
import androidx.compose.foundation.layout.padding
class MainActivity : ComponentActivity() {

    private val appContainer by lazy { AppContainer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PatientsTheme {
                val patientViewModel: PatientViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(
                            modelClass: Class<T>
                        ): T {
                            if (modelClass.isAssignableFrom(
                                    PatientViewModel::class.java
                                )
                            ) {
                                @Suppress("UNCHECKED_CAST")
                                return PatientViewModel(
                                    appContainer.patientRepository
                                ) as T
                            }
                            throw IllegalArgumentException(
                                "ViewModel inconnu"
                            )
                        }
                    }
                )

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        PatientsScreen(viewModel = patientViewModel)
                    }
                }
            }
        }
    }
}
