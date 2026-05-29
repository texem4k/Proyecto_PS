package software.ulpgc.code.application.ui.wizard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
data class WizardStep(val label: String)


@Composable
fun WizardHeader(
    title: String,
    step: Int,
    steps: List<WizardStep>,
    onClose: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Text("✖\uFE0E", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, wizardStep ->
                StepIndicator(
                    index = index,
                    current = step,
                    label = wizardStep.label
                )
                if (index < steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(1.dp)
                            .background(
                                if (index < step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
        }
    }
}
@Composable
fun WizardNavigation(
    step: Int,
    totalSteps: Int,
    submitLabel: String,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (step > 0) {
            OutlinedButton(onClick = onBack) {
                Text("← Anterior", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        } else {
            Spacer(Modifier.width(1.dp))
        }

        if (step < totalSteps - 1) {
            Button(onClick = onNext) {
                Text("Siguiente →")
            }
        } else {
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(
                    1.dp, MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(submitLabel)
            }
        }
    }
}

@Composable
fun StepIndicator(index: Int, current: Int, label: String) {
    val isDone   = index < current
    val isActive = index == current
    val circleColor = when {
        isDone || isActive -> MaterialTheme.colorScheme.primary
        else               -> MaterialTheme.colorScheme.primaryContainer
    }
    val textColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isDone   -> MaterialTheme.colorScheme.onSurfaceVariant
        else     -> MaterialTheme.colorScheme.primaryContainer
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isDone || isActive) circleColor else Color.Transparent)
                .border(1.5.dp, circleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isDone) "✓" else "${index + 1}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDone || isActive) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.outlineVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StepLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}