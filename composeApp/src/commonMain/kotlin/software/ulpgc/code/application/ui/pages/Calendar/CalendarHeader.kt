package software.ulpgc.code.application.ui.pages.Calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.kizitonwose.calendar.compose.yearcalendar.YearCalendarState
import com.kizitonwose.calendar.core.CalendarYear

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
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null)
        }
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
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
                    tint = Color.Gray
                )
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
    onFilterClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(yearState.isScrollInProgress) {
        if (yearState.isScrollInProgress) expanded = false
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousYear) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Año anterior")
            }
            Text(
                text = year.year.value.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = onNextYear) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Año siguiente")
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onFilterClick) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtrar tareas",
                    tint = Color.Gray
                )
            }

            Box(contentAlignment = Alignment.CenterEnd) {
                Button(onClick = { expanded = true }) {
                    Text(text = viewMode.name)
                }
                if (expanded) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(10, 60),
                        onDismissRequest = { expanded = false }
                    ) {
                        Column(
                            modifier = Modifier
                                .width(150.dp)
                                .wrapContentHeight()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            CalendarViewMode.entries.forEach { mode ->
                                Text(
                                    text = mode.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onViewModeChange(mode)
                                            expanded = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
