package software.ulpgc.code.application.ui.pages.Calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.yearcalendar.YearCalendarState
import com.kizitonwose.calendar.core.CalendarYear
import software.ulpgc.code.application.ui.DropdownCustom
import software.ulpgc.code.application.ui.DropdownSelection

@Composable
fun CalendarHeader(
    title: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            LegendDropdown(scrollState = scrollState)
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            NavigationSection(
                title = title,
                onPreviousClick = onPreviousClick,
                onNextClick = onNextClick
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            FilterAndViewMode(
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
                onFilterClick = onFilterClick,
                scrollState = scrollState
            )
        }
    }
}

@Composable
private fun LegendDropdown(scrollState: androidx.compose.foundation.ScrollState) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value) {
        if (expanded) expanded = false
    }

    Box() {
        Button(onClick = { expanded = true }) {
            Text(text = "Leyenda")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            CalendarConstants.LEGEND_ITEMS_CALENDAR.forEach { (text, color) ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(16.dp).background(color))
                            Spacer(Modifier.width(8.dp))
                            Text(text)
                        }
                    },
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun NavigationSection(
    title: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun FilterAndViewMode(
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onFilterClick: () -> Unit,
    scrollState: androidx.compose.foundation.ScrollState
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value) {
        if (expanded) expanded = false
    }

    Box(contentAlignment = Alignment.TopEnd) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onFilterClick) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtrar tareas",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Box {
                Button(onClick = { expanded = true }) {
                    Text(text = viewMode.name)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    CalendarViewMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name) },
                            onClick = {
                                onViewModeChange(mode)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun YearHeader(
    year: CalendarYear,
    yearState: YearCalendarState,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onFilterClick: () -> Unit,
    scrollState: androidx.compose.foundation.ScrollState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            LegendDropdown(scrollState = scrollState)
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousYear) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Año anterior", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text(
                    text = year.year.value.toString(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 26.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onNextYear) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Año siguiente", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            FilterAndViewMode(
                viewMode = viewMode,
                onViewModeChange = onViewModeChange,
                onFilterClick = onFilterClick,
                scrollState = scrollState
            )
        }

    }
}
