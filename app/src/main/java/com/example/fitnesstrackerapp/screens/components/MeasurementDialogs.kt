package com.example.fitnesstrackerapp.screens.components

/**
 * Dialog components for measurement input.
 *
 * Contains composable dialogs for entering and updating user measurements such as height and weight.
 * Each dialog provides input validation and user-friendly UI for data entry.
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Dialog for entering or editing the user's height.
 *
 * @param isVisible Whether the dialog should be shown
 * @param currentHeight The current height value to prefill
 * @param onHeightChange Callback when a valid height is entered and saved
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
fun HeightInputDialog(
    isVisible: Boolean,
    currentHeight: Float,
    onHeightChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    if (isVisible) {
        // State for the height input field, initialized with the current height
        var heightText by remember { mutableStateOf(currentHeight.toString()) }

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier.padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Enter Height",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input field for height in centimeters
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons: Cancel and Save
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                // Validate input and call onHeightChange if valid
                                heightText.toFloatOrNull()?.let { height ->
                                    onHeightChange(height)
                                    onDismiss()
                                }
                            },
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog for entering or editing the user's weight.
 *
 * @param isVisible Whether the dialog should be shown
 * @param currentWeight The current weight value to prefill
 * @param onWeightChange Callback when a valid weight is entered and saved
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
fun WeightInputDialog(
    isVisible: Boolean,
    currentWeight: Float,
    onWeightChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    if (isVisible) {
        // State for the weight input field, initialized with the current weight
        var weightText by remember { mutableStateOf(currentWeight.toString()) }

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier.padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Enter Weight",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input field for weight in kilograms
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons: Cancel and Save
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                // Validate input and call onWeightChange if valid
                                weightText.toFloatOrNull()?.let { weight ->
                                    onWeightChange(weight)
                                    onDismiss()
                                }
                            },
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
