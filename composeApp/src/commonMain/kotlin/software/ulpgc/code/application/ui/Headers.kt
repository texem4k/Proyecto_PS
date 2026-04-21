package software.ulpgc.code.application.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Headers(onNavigate: (Screen) -> Unit, value: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text(
            text = value,
            modifier = Modifier.align(Alignment.Center)
        )
        Button(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = { onNavigate(Screen.HOME) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        ) {
            Text("✖\uFE0E")
        }
    }
}
