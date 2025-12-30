package com.example.proto7hive.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SignUpFlowScreen(
    viewModel: SignUpViewModel = viewModel(),
    onSignUpComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    
    when (state.currentStep) {
        1 -> SignUpStep1Screen(
            viewModel = viewModel,
            onNext = { viewModel.nextStep() }
        )
        2 -> SignUpStep2Screen(
            viewModel = viewModel,
            onNext = { viewModel.nextStep() }
        )
        3 -> SignUpStep3Screen(
            viewModel = viewModel,
            onNext = { viewModel.nextStep() }
        )
        4 -> SignUpStep4Screen(
            viewModel = viewModel,
            onNext = { viewModel.nextStep() }
        )
        5 -> SignUpStep5Screen(
            viewModel = viewModel,
            onNext = { viewModel.nextStep() }
        )
        6 -> SignUpStep6Screen(
            viewModel = viewModel,
            onSignUpComplete = onSignUpComplete
        )
    }
}

