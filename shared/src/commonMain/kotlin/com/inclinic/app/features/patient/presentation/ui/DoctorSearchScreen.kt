package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.BadgeCheck
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.Star
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.features.patient.presentation.component.DoctorSearchComponent
import com.inclinic.app.features.patient.presentation.component.DoctorSearchState
import com.inclinic.app.features.patient.presentation.component.DoctorSortOrder
import com.inclinic.app.ui.atoms.ChipSpecialty
import com.inclinic.app.ui.atoms.PatientTab
import com.inclinic.app.ui.atoms.SearchBar
import com.inclinic.app.ui.theme.AppTheme

private val specialties = listOf(null, "Cardiología", "Dermatología", "Nutrición")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSearchScreen(
    component: DoctorSearchComponent,
    onNavTabSelected: (PatientTab) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state     by component.state.subscribeAsState()
        val listState  = rememberLazyListState()
        val colors     = AppTheme.colors
        val typography = AppTheme.typography

        var showFilters by remember { mutableStateOf(false) }
        val sheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val hasActiveFilters = state.minPrice != null || state.maxPrice != null ||
            state.minRating != null || state.offersTelemedicine != null ||
            state.offersHomeVisit != null || state.sortOrder != DoctorSortOrder.Recent

        val shouldLoadMore by remember {
            derivedStateOf {
                val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val total = listState.layoutInfo.totalItemsCount
                total > 0 && last >= total - 3 && state.hasMore
            }
        }
        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) component.onLoadMore()
        }

        Box(modifier = modifier.fillMaxSize().background(colors.sand)) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                // ── Header + search + filters ─────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 20.dp, end = 20.dp),
                ) {
                    // Title row
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text  = "Buscar Doctores",
                            style = typography.body.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                            color = colors.text,
                        )
                        Box(
                            contentAlignment = Alignment.TopEnd,
                            modifier         = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication        = null,
                                    onClick           = { showFilters = true },
                                )
                                .padding(6.dp),
                        ) {
                            Icon(
                                imageVector        = Lucide.SlidersHorizontal,
                                contentDescription = "Filtros avanzados",
                                tint               = colors.navy,
                                modifier           = Modifier.size(22.dp),
                            )
                            if (hasActiveFilters) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(colors.navy),
                                )
                            }
                        }
                    }

                    // Search field
                    SearchBar(
                        query         = state.query,
                        onQueryChange = component::onQueryChange,
                        placeholder   = "Nombre, especialidad...",
                    )

                    // Specialty chips — horizontally scrollable
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier              = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        specialties.forEach { specialty ->
                            val isSelected = state.selectedSpecialty == specialty
                            ChipSpecialty(
                                label    = specialty ?: "Todas",
                                selected = isSelected,
                                onClick  = { component.onSpecialtyChange(specialty) },
                            )
                        }
                    }

                    // Sort row
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text  = "Ordenar:",
                            style = typography.body.copy(fontSize = 12.sp),
                            color = colors.text,
                        )
                        SortChip(
                            label    = "Más recientes",
                            isActive = state.sortOrder == DoctorSortOrder.Recent,
                            onClick  = { component.onSortChange(DoctorSortOrder.Recent) },
                        )
                        SortChip(
                            label    = "Mejor calificados",
                            isActive = state.sortOrder == DoctorSortOrder.TopRated,
                            onClick  = { component.onSortChange(DoctorSortOrder.TopRated) },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Results list ─────────────────────────────────────────────
                LazyColumn(
                    state                  = listState,
                    verticalArrangement    = Arrangement.spacedBy(16.dp),
                    modifier               = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                ) {
                    items(state.results, key = { it.id }) { doctor ->
                        DoctorResultCard(
                            doctor  = doctor,
                            onClick = { component.onDoctorTapped(doctor.id) },
                        )
                    }

                    if (state.isLoading) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier         = Modifier.fillMaxWidth().padding(16.dp),
                            ) {
                                CircularProgressIndicator(
                                    color    = colors.navy,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }

                    if (!state.isLoading && state.results.isEmpty()) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier         = Modifier.fillMaxWidth().padding(32.dp),
                            ) {
                                Text(
                                    text  = "No se encontraron doctores",
                                    style = typography.body,
                                    color = colors.muted,
                                )
                            }
                        }
                    }

                    // Bottom spacing before NavBar
                    item { Spacer(Modifier.height(4.dp)) }
                }

            }

            if (showFilters) {
                AdvancedFilterSheet(
                    initial    = state,
                    sheetState = sheetState,
                    onApply    = { minP, maxP, rating, telemed, home, sort ->
                        component.onApplyFilters(minP, maxP, rating, telemed, home, sort)
                        showFilters = false
                    },
                    onReset    = {
                        component.onResetFilters()
                        showFilters = false
                    },
                    onDismiss  = { showFilters = false },
                )
            }
        }
    }
}

// ── Doctor result card ────────────────────────────────────────────────────────

@Composable
private fun DoctorResultCard(
    doctor: Doctor,
    onClick: () -> Unit,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier            = Modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 4.dp,
                shape        = RoundedCornerShape(16.dp),
                ambientColor = Color(0x15000000),
                spotColor    = Color(0x15000000),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(colors.elevated)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(16.dp),
    ) {
        // Top row: avatar + name + verified
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Avatar initials
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.navy),
            ) {
                Text(
                    text       = doctor.fullName.initials(),
                    color      = Color.White,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier            = Modifier.weight(1f),
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text  = doctor.fullName,
                        style = typography.body.copy(fontWeight = FontWeight.Bold),
                        color = colors.text,
                    )
                    Icon(
                        imageVector        = Lucide.BadgeCheck,
                        contentDescription = null,
                        tint               = colors.navy,
                        modifier           = Modifier.size(16.dp),
                    )
                }
            }
        }

        // Specialty tags
        if (doctor.specialties.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                doctor.specialties.take(2).forEach { spec ->
                    SpecialtyTag(name = spec.name)
                }
            }
        }

        // Bottom row: price + rating | Ver perfil button
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier              = Modifier.fillMaxWidth(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text  = "S/. ${doctor.consultationFee}",
                    style = typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    color = colors.text,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(Lucide.Star, contentDescription = null, tint = colors.amber, modifier = Modifier.size(11.dp))
                    val rating = doctor.ratingAverage
                    if (rating != null) {
                        Text(
                            text  = "$rating · ${doctor.ratingsCount} reseñas",
                            style = typography.body.copy(fontSize = 11.sp),
                            color = colors.text,
                        )
                    } else {
                        Text(
                            text  = "Sin reseñas",
                            style = typography.body.copy(fontSize = 11.sp),
                            color = colors.muted,
                        )
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.navy)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text       = "Ver perfil",
                    color      = Color.White,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ── Chips ─────────────────────────────────────────────────────────────────────

@Composable
private fun SortChip(label: String, isActive: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val bgColor     = if (isActive) colors.navy else colors.elevated
    val textColor   = if (isActive) Color.White else colors.text
    val borderColor = if (isActive) Color.Transparent else colors.border

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .then(
                if (!isActive) Modifier.border(1.dp, borderColor, RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text       = label,
            color      = textColor,
            fontSize   = 11.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun SpecialtyTag(name: String) {
    val colors = AppTheme.colors
    // Map known specialties to their design colors
    val (bg, fg) = when {
        name.contains("Nutri",  ignoreCase = true) -> colors.tealBg  to colors.teal
        name.contains("Cardio", ignoreCase = true) -> colors.redBg   to colors.red
        name.contains("Psico",  ignoreCase = true) -> colors.purpleBg to colors.purple
        name.contains("Fisiote",ignoreCase = true) -> colors.infoBg to colors.info
        name.contains("Dermato",ignoreCase = true) -> colors.amberBg to colors.amber
        else                                        -> colors.navyTint to colors.navy
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text       = name,
            color      = fg,
            fontSize   = 10.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun String.initials(): String {
    val parts = trim().split(" ").filter { it.isNotBlank() && it.lowercase() != "dr." && it.lowercase() != "dra." }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.isNotEmpty() -> parts[0].take(2).uppercase()
        else -> "DR"
    }
}

private fun Double.priceLabel(): String =
    if (this % 1.0 == 0.0) toInt().toString() else toString()

// ── Advanced filter sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedFilterSheet(
    initial: DoctorSearchState,
    sheetState: SheetState,
    onApply: (
        minPrice: Double?,
        maxPrice: Double?,
        minRating: Double?,
        offersTelemedicine: Boolean?,
        offersHomeVisit: Boolean?,
        sortOrder: DoctorSortOrder,
    ) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors

    // Draft state — committed only on "Aplicar" so sliding/typing doesn't fire searches.
    var minPriceText by remember { mutableStateOf(initial.minPrice?.priceLabel() ?: "") }
    var maxPriceText by remember { mutableStateOf(initial.maxPrice?.priceLabel() ?: "") }
    var rating       by remember { mutableStateOf(initial.minRating) }
    var telemed      by remember { mutableStateOf(initial.offersTelemedicine == true) }
    var homeVisit    by remember { mutableStateOf(initial.offersHomeVisit == true) }
    var sort         by remember { mutableStateOf(initial.sortOrder) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colors.elevated,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier            = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
        ) {
            Text(
                text  = "Filtros avanzados",
                style = AppTheme.typography.body.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )

            // Price range
            FilterSection(title = "Precio (S/.)") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    PriceField(label = "Mínimo", value = minPriceText, onValueChange = { minPriceText = it })
                    PriceField(label = "Máximo", value = maxPriceText, onValueChange = { maxPriceText = it })
                }
            }

            // Minimum rating
            FilterSection(title = "Calificación mínima") {
                RatingStarsSelector(rating = rating, onRatingChange = { rating = it })
            }

            // Visit-type toggles
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterToggleRow(label = "Atención por telemedicina", checked = telemed, onCheckedChange = { telemed = it })
                FilterToggleRow(label = "Visita a domicilio", checked = homeVisit, onCheckedChange = { homeVisit = it })
            }

            // Sort
            FilterSection(title = "Ordenar por") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                ) {
                    SortChip("Más recientes", sort == DoctorSortOrder.Recent) { sort = DoctorSortOrder.Recent }
                    SortChip("Mejor calificados", sort == DoctorSortOrder.TopRated) { sort = DoctorSortOrder.TopRated }
                    SortChip("Precio: menor", sort == DoctorSortOrder.PriceAsc) { sort = DoctorSortOrder.PriceAsc }
                    SortChip("Precio: mayor", sort == DoctorSortOrder.PriceDesc) { sort = DoctorSortOrder.PriceDesc }
                }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onReset,
                        )
                        .padding(vertical = 14.dp),
                ) {
                    Text("Limpiar", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.navy)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = {
                                onApply(
                                    minPriceText.toDoubleOrNull(),
                                    maxPriceText.toDoubleOrNull(),
                                    rating,
                                    telemed.takeIf { it },
                                    homeVisit.takeIf { it },
                                    sort,
                                )
                            },
                        )
                        .padding(vertical = 14.dp),
                ) {
                    Text("Aplicar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(title, color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun RowScope.PriceField(label: String, value: String, onValueChange: (String) -> Unit) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
        Text(label, color = colors.muted, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.sand)
                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            BasicTextField(
                value           = value,
                onValueChange   = { input -> onValueChange(input.filter { it.isDigit() }) },
                singleLine      = true,
                textStyle       = TextStyle(color = colors.text, fontSize = 14.sp),
                cursorBrush     = SolidColor(colors.navy),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox   = { inner ->
                    if (value.isEmpty()) {
                        Text("0", color = colors.light, fontSize = 14.sp)
                    }
                    inner()
                },
            )
        }
    }
}

@Composable
private fun RatingStarsSelector(rating: Double?, onRatingChange: (Double?) -> Unit) {
    val colors = AppTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).forEach { star ->
            val filled = (rating ?: 0.0) >= star
            Icon(
                imageVector        = Lucide.Star,
                contentDescription = "$star estrellas o más",
                tint               = if (filled) colors.amber else colors.light,
                modifier           = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = {
                            onRatingChange(if (rating == star.toDouble()) null else star.toDouble())
                        },
                    ),
            )
        }
    }
}

@Composable
private fun FilterToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = AppTheme.colors
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier              = Modifier.fillMaxWidth(),
    ) {
        Text(label, color = colors.text, fontSize = 14.sp)
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors          = SwitchDefaults.colors(checkedTrackColor = colors.navy),
        )
    }
}

