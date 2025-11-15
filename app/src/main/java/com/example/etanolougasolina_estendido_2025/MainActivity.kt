package com.example.etanolougasolina_estendido_2025

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.etanolougasolina_estendido_2025.ui.theme.EtanolOuGasolina_EstendidoTheme
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.filled.LocationOn
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import android.content.Context
import kotlinx.serialization.json.Json
import androidx.core.content.edit
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import java.text.DateFormat
import java.util.Date



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemNavigationBar()
        setContent {
            EtanolOuGasolina_EstendidoTheme {
                EtanolOuGasolina_ExtendidoApp()
            }
        }
    }

    private fun hideSystemNavigationBar() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }
}

@PreviewScreenSizes
@Composable
fun EtanolOuGasolina_ExtendidoApp() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ESTADO PRINCIPAL

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.CALCULATOR) }
    var valoretanol by rememberSaveable { mutableStateOf("") }
    var valorgasolina by rememberSaveable { mutableStateOf("") }

    var eficiencia by rememberSaveable {
        mutableFloatStateOf(
            prefs.getFloat(PREF_KEY_EFFICIENCY, 0.7f)
        )
    }

    var favorites by rememberSaveable {
        mutableStateOf(
            prefs.getString(PREF_KEY_FAVORITES, null)?.let { json ->
                runCatching {
                    Json.decodeFromString<List<FavoriteStation>>(json)
                }.getOrElse { emptyList() }
            } ?: emptyList()
        )
    }
    var selectedFavoriteIds by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var editingFavorite by remember { mutableStateOf<FavoriteStation?>(null) }

    val isSelectionMode = selectedFavoriteIds.isNotEmpty()

    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var stationNameInput by rememberSaveable { mutableStateOf("") }

    val opts = listOf(0.7f, 0.75f)

    // CÁLCULOS DA TELA PRINCIPAL

    val etanolDouble = valoretanol.replace(',', '.').toDoubleOrNull() ?: 0.0
    val gasolinaDouble = valorgasolina.replace(',', '.').toDoubleOrNull() ?: 0.0

    val razao = if (gasolinaDouble > 0) etanolDouble / gasolinaDouble else 0.0
    val recomend = if (gasolinaDouble > 0 && razao <= eficiencia) {
        stringResource(R.string.recommendation_ethanol)
    } else {
        stringResource(R.string.recommendation_gasoline)
    }

    val formatPercent = NumberFormat.getPercentInstance(Locale.getDefault())
    val razaoFormat = formatPercent.format(razao)
    val eficienciaFormat = formatPercent.format(eficiencia)

    // LOCALIZAÇÃO

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var hasLocationPermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                FINE_LOCATION_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var lastLocation by remember { mutableStateOf<Location?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lastLocation = location
                }
            }
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lastLocation = location
                }
            }
        }
    }

    // LÓGICA PARA SALVAR FAVORITO

    val canSave = etanolDouble > 0.0 && gasolinaDouble > 0.0

    val onSaveFavorite: () -> Unit = onSaveFavorite@{
        if (!canSave) {
            Toast.makeText(
                context,
                context.getString(R.string.error_fill_values_before_saving),
                Toast.LENGTH_SHORT
            ).show()
            return@onSaveFavorite
        }

        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(FINE_LOCATION_PERMISSION)
        } else {
            val nextId = favorites.size + 1
            stationNameInput = context.getString(R.string.station_default_name, nextId)
            showSaveDialog = true
        }
    }

    fun toggleFavoriteSelection(station: FavoriteStation) {
        val id = station.id
        selectedFavoriteIds = if (id in selectedFavoriteIds) {
            selectedFavoriteIds - id
        } else {
            selectedFavoriteIds + id
        }
    }

    fun handleFavoriteClick(station: FavoriteStation) {
        if (isSelectionMode) {
            toggleFavoriteSelection(station)
        } else {
            editingFavorite = station
        }
    }

    fun handleFavoriteLongClick(station: FavoriteStation) {
        toggleFavoriteSelection(station)
    }

    fun deleteSelectedFavorites() {
        if (selectedFavoriteIds.isNotEmpty()) {
            val idsToDelete = selectedFavoriteIds.toSet()
            favorites = favorites.filterNot { it.id in idsToDelete }
            selectedFavoriteIds = emptyList()
        }
    }

    LaunchedEffect(eficiencia) {
        prefs.edit {
            putFloat(PREF_KEY_EFFICIENCY, eficiencia)
        }
    }

    LaunchedEffect(favorites) {
        val json = Json.encodeToString(favorites)
        prefs.edit {
            putString(PREF_KEY_FAVORITES, json)
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = stringResource(destination.labelRes)
                        )
                    },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (currentDestination) {
                AppDestinations.CALCULATOR -> {
                    Calculadora(
                        valoretanol = valoretanol,
                        onValoretanolChange = { valoretanol = it },
                        valorgasolina = valorgasolina,
                        onValorgasolinaChange = { valorgasolina = it },
                        eficiencia = eficiencia,
                        onEficienciaChange = { eficiencia = it },
                        opts = opts,
                        razaoFormat = razaoFormat,
                        eficienciaFormat
                        = eficienciaFormat,
                        recomend = recomend,
                        onSaveFavorite = onSaveFavorite
                    )
                }

                AppDestinations.FAVORITES -> {
                    FavoritesScreen(
                        favorites = favorites,
                        selectedIds = selectedFavoriteIds,
                        isSelectionMode = isSelectionMode,
                        onCardClick = { station -> handleFavoriteClick(station) },
                        onCardLongClick = { station -> handleFavoriteLongClick(station) },
                        onDeleteSelected = { deleteSelectedFavorites() },
                        onOpenLocation = { station ->
                            val lat = station.latitude
                            val lon = station.longitude

                            if (lat == null || lon == null) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.error_location_unavailable),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val uri =
                                    "geo:$lat,$lon?q=$lat,$lon(${Uri.encode(station.name)})".toUri()
                                val intent = Intent(Intent.ACTION_VIEW, uri)

                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.error_open_maps),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                }
            }

            if (showSaveDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showSaveDialog = false
                        stationNameInput = ""
                    },
                    title = { Text(text = stringResource(R.string.dialog_save_station_title)) },
                    text = {
                        OutlinedTextField(
                            value = stationNameInput,
                            onValueChange = { stationNameInput = it },
                            singleLine = true,
                            label = { Text(stringResource(R.string.dialog_station_name_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (canSave) {
                                    val nextId = favorites.size + 1
                                    val finalName =
                                        stationNameInput.ifBlank {
                                            context.getString(
                                                R.string.station_default_name,
                                                nextId
                                            )
                                        }

                                    favorites = favorites + FavoriteStation(
                                        id = nextId,
                                        name = finalName,
                                        ethanolPrice = etanolDouble,
                                        gasolinePrice = gasolinaDouble,
                                        efficiency = eficiencia,
                                        ratio = razao,
                                        recommendation = recomend,
                                        latitude = lastLocation?.latitude,
                                        longitude = lastLocation?.longitude,
                                        createdAt = System.currentTimeMillis()
                                    )

                                    valoretanol = ""
                                    valorgasolina = ""
                                }
                                stationNameInput = ""
                                showSaveDialog = false
                            }
                        ) {
                            Text(stringResource(R.string.dialog_save))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                stationNameInput = ""
                                showSaveDialog = false
                            }
                        ) {
                            Text(stringResource(R.string.dialog_cancel))
                        }
                    }
                )
            }

            if (editingFavorite != null) {
                val station = editingFavorite!!

                var name by remember(station.id) { mutableStateOf(station.name) }
                var etanolText by remember(station.id) {
                    mutableStateOf(station.ethanolPrice.toString())
                }
                var gasolinaText by remember(station.id) {
                    mutableStateOf(station.gasolinePrice.toString())
                }
                var localEfficiency by remember(station.id) {
                    mutableFloatStateOf(station.efficiency)
                }

                AlertDialog(
                    onDismissRequest = { editingFavorite = null },
                    title = {
                        Text(stringResource(R.string.dialog_edit_station_title))
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                singleLine = true,
                                label = {
                                    Text(
                                        stringResource(
                                            R.string.dialog_station_name_label
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = etanolText,
                                onValueChange = { etanolText = it },
                                singleLine = true,
                                label = {
                                    Text(
                                        stringResource(
                                            R.string.dialog_ethanol_value_label
                                        )
                                    )
                                },
                                prefix = {
                                    Text(
                                        stringResource(
                                            R.string.currency_symbol
                                        )
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = gasolinaText,
                                onValueChange = { gasolinaText = it },
                                singleLine = true,
                                label = {
                                    Text(
                                        stringResource(
                                            R.string.dialog_gasoline_value_label
                                        )
                                    )
                                },
                                prefix = {
                                    Text(
                                        stringResource(
                                            R.string.currency_symbol
                                        )
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.efficiency_criterion_with_value,
                                        (localEfficiency * 100).toInt()
                                    ),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Switch(
                                    checked = localEfficiency >= 0.75f,
                                    onCheckedChange = { checked ->
                                        localEfficiency =
                                            if (checked) 0.75f else 0.7f
                                    }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val etanol =
                                    etanolText.replace(',', '.').toDoubleOrNull()
                                val gasolina =
                                    gasolinaText.replace(',', '.').toDoubleOrNull()

                                if (etanol != null && gasolina != null && gasolina > 0.0) {
                                    val newRatio = etanol / gasolina
                                    val newRecommendation =
                                        if (newRatio <= localEfficiency) {
                                            context.getString(
                                                R.string.recommendation_ethanol
                                            )
                                        } else {
                                            context.getString(
                                                R.string.recommendation_gasoline
                                            )
                                        }

                                    favorites = favorites.map {
                                        if (it.id == station.id) {
                                            it.copy(
                                                name = name.ifBlank { station.name },
                                                ethanolPrice = etanol,
                                                gasolinePrice = gasolina,
                                                efficiency = localEfficiency,
                                                ratio = newRatio,
                                                recommendation = newRecommendation
                                            )
                                        } else it
                                    }
                                    editingFavorite = null
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.error_invalid_values),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        ) {
                            Text(stringResource(R.string.dialog_save))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingFavorite = null }) {
                            Text(stringResource(R.string.dialog_cancel))
                        }
                    }
                )
            }
        }
    }
}

enum class AppDestinations(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    CALCULATOR(R.string.dest_calculator, Icons.Default.Calculate),
    FAVORITES(R.string.dest_favorites, Icons.Default.LocalGasStation),
}

@Composable
fun Calculadora(
    modifier: Modifier = Modifier,
    valoretanol: String,
    onValoretanolChange: (String) -> Unit,
    valorgasolina: String,
    onValorgasolinaChange: (String) -> Unit,
    eficiencia: Float,
    onEficienciaChange: (Float) -> Unit,
    opts: List<Float>,
    razaoFormat: String,
    eficienciaFormat: String,
    recomend: String,
    onSaveFavorite: () -> Unit
) {
    val configuration = LocalConfiguration.current

    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            CalculadoraLandscape(
                valoretanol = valoretanol,
                onValoretanolChange = onValoretanolChange,
                valorgasolina = valorgasolina,
                onValorgasolinaChange = onValorgasolinaChange,
                eficiencia = eficiencia,
                onEficienciaChange = onEficienciaChange,
                opts = opts,
                razaoFormat = razaoFormat,
                eficienciaFormat = eficienciaFormat,
                recomend = recomend,
                onSaveFavorite = onSaveFavorite,
                modifier = modifier
            )
        }

        else -> {
            CalculadoraPortrait(
                valoretanol = valoretanol,
                onValoretanolChange = onValoretanolChange,
                valorgasolina = valorgasolina,
                onValorgasolinaChange = onValorgasolinaChange,
                eficiencia = eficiencia,
                onEficienciaChange = onEficienciaChange,
                opts = opts,
                razaoFormat = razaoFormat,
                eficienciaFormat = eficienciaFormat,
                recomend = recomend,
                onSaveFavorite = onSaveFavorite,
                modifier = modifier
            )
        }
    }
}

@Composable
fun CalculadoraPortrait(
    modifier: Modifier = Modifier,
    valoretanol: String,
    onValoretanolChange: (String) -> Unit,
    valorgasolina: String,
    onValorgasolinaChange: (String) -> Unit,
    eficiencia: Float,
    onEficienciaChange: (Float) -> Unit,
    opts: List<Float>,
    razaoFormat: String,
    eficienciaFormat: String,
    recomend: String,
    onSaveFavorite: () -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp, 28.dp, 8.dp, 8.dp)
                .weight(7f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp, 14.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.calculator_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(.625f),
                    autoSize = TextAutoSize.StepBased(),
                )
                Text(
                    text = stringResource(R.string.calculator_subtitle),
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Thin),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.625f),
                    autoSize = TextAutoSize.StepBased()
                )
                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    text = stringResource(R.string.ethanol_value_label),
                    style = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.325f),
                    autoSize = TextAutoSize.StepBased()
                )

                OutlinedTextField(
                    value = valoretanol,
                    onValueChange = { onValoretanolChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.15f),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(stringResource(R.string.ethanol_value_label)) },
                    prefix = { Text(stringResource(R.string.currency_symbol)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    text = stringResource(R.string.gasoline_value_label),
                    style = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.325f),
                    autoSize = TextAutoSize.StepBased()
                )

                OutlinedTextField(
                    value = valorgasolina,
                    onValueChange = { onValorgasolinaChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.15f),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(stringResource(R.string.gasoline_value_label)) },
                    prefix = { Text(stringResource(R.string.currency_symbol)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.weight(.1f))

                Text(
                    text = stringResource(R.string.efficiency_criterion_title),
                    style = TextStyle(
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.325f),
                    autoSize = TextAutoSize.StepBased(),
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    opts.forEachIndexed { i, v ->
                        SegmentedButton(
                            selected = (eficiencia == v),
                            onClick = { onEficienciaChange(v) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = i,
                                count = opts.size,
                                baseShape = RoundedCornerShape(16.dp)
                            ),
                            modifier = Modifier.fillMaxHeight(fraction = 0.75f),
                        ) {
                            Text(
                                stringResource(
                                    R.string.efficiency_option_format,
                                    (v * 100).toInt()
                                )
                            )
                        }
                    }
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp, 8.dp, 8.dp, 8.dp)
                .weight(3f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp, 14.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.result_title),
                    style = TextStyle(fontSize = 18.sp),
                    autoSize = TextAutoSize.StepBased(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.75f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.ratio_between_values_label),
                        style = TextStyle(
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.Light
                        ),
                        modifier = Modifier.weight(2f),
                        autoSize = TextAutoSize.StepBased(),
                    )
                    Text(
                        text = razaoFormat,
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Light
                        ),
                        modifier = Modifier.weight(1f),
                        autoSize = TextAutoSize.StepBased(),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.efficiency_criterion_row_label),
                        style = TextStyle(
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.Light
                        ),
                        modifier = Modifier.weight(2f),
                        autoSize = TextAutoSize.StepBased(),
                    )
                    Text(
                        text = eficienciaFormat,
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Light
                        ),
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        autoSize = TextAutoSize.StepBased(),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.25f)
                ) {
                    Text(
                        text = stringResource(R.string.recommendation_label),
                        style = TextStyle(textAlign = TextAlign.Left),
                        modifier = Modifier.weight(2f),
                        autoSize = TextAutoSize.StepBased(),
                    )
                }
                Card(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxSize(),
                    shape = RoundedCornerShape(999.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inversePrimary,
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = recomend,
                            style = TextStyle(textAlign = TextAlign.Center),
                            autoSize = TextAutoSize.StepBased(),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
        Button(
            onClick = onSaveFavorite,
            modifier = modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp, 8.dp, 8.dp, 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = stringResource(R.string.save_station_button),
                style = TextStyle(textAlign = TextAlign.Center),
                autoSize = TextAutoSize.StepBased(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun CalculadoraLandscape(
    modifier: Modifier = Modifier,
    valoretanol: String,
    onValoretanolChange: (String) -> Unit,
    valorgasolina: String,
    onValorgasolinaChange: (String) -> Unit,
    eficiencia: Float,
    onEficienciaChange: (Float) -> Unit,
    opts: List<Float>,
    razaoFormat: String,
    eficienciaFormat: String,
    recomend: String,
    onSaveFavorite: () -> Unit
) {

    Row(
        modifier = modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp, 28.dp, 4.dp, 8.dp)
                .weight(1f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp, 14.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.calculator_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(.7f),
                    autoSize = TextAutoSize.StepBased(),
                )
                Text(
                    text = stringResource(R.string.calculator_subtitle),
                    style = TextStyle(fontWeight = FontWeight.Thin),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.35f),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.325f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.ethanol_value_label) + ":",
                        modifier = Modifier
                            .weight(2f),
                        textAlign = TextAlign.Left,
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(10.sp, 25.sp)
                    )
                    TextField(
                        value = valoretanol,
                        onValueChange = { onValoretanolChange(it) },
                        modifier = Modifier
                            .weight(1.75f),
                        singleLine = true,
                        label = { Text(stringResource(R.string.ethanol_value_label)) },
                        prefix = { Text(stringResource(R.string.currency_symbol)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.325f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.gasoline_value_label) + ":",
                        modifier = Modifier.weight(2f),
                        textAlign = TextAlign.Left,
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(10.sp, 25.sp)
                    )

                    TextField(
                        value = valorgasolina,
                        onValueChange = { onValorgasolinaChange(it) },
                        modifier = Modifier
                            .weight(1.75f),
                        singleLine = true,
                        label = { Text(stringResource(R.string.gasoline_value_label)) },
                        prefix = { Text(stringResource(R.string.currency_symbol)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.125f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.efficiency_criterion_row_label),
                        style = TextStyle(
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                            .padding(end = 4.dp),
                        autoSize = TextAutoSize.StepBased(),
                        maxLines = 1
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxWidth()
                    ) {
                        opts.forEachIndexed { i, v ->
                            SegmentedButton(
                                selected = (eficiencia == v),
                                onClick = { onEficienciaChange(v) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = i,
                                    count = opts.size,
                                    baseShape = RoundedCornerShape(16.dp)
                                ),
                                modifier = Modifier.fillMaxHeight(fraction = 0.75f),
                            ) {
                                Text(
                                    stringResource(
                                        R.string.efficiency_option_format,
                                        (v * 100).toInt()
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp, 28.dp, 8.dp, 2.dp)
                    .weight(3f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp, 14.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.result_title),
                        style = TextStyle(fontSize = 18.sp),
                        autoSize = TextAutoSize.StepBased(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.75f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.ratio_between_values_label),
                            style = TextStyle(
                                textAlign = TextAlign.Left,
                                fontWeight = FontWeight.Light
                            ),
                            modifier = Modifier.weight(2f),
                            autoSize = TextAutoSize.StepBased(),
                        )
                        Text(
                            text = razaoFormat,
                            style = TextStyle(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Light
                            ),
                            modifier = Modifier.weight(1f),
                            autoSize = TextAutoSize.StepBased(),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.efficiency_criterion_row_label),
                            style = TextStyle(
                                textAlign = TextAlign.Left,
                                fontWeight = FontWeight.Light
                            ),
                            modifier = Modifier.weight(2f),
                            autoSize = TextAutoSize.StepBased(),
                        )
                        Text(
                            text = eficienciaFormat,
                            style = TextStyle(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Light
                            ),
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            autoSize = TextAutoSize.StepBased(),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.25f)
                    ) {
                        Text(
                            text = stringResource(R.string.recommendation_label),
                            style = TextStyle(textAlign = TextAlign.Left),
                            modifier = Modifier.weight(2f),
                            autoSize = TextAutoSize.StepBased(),
                        )
                    }
                    Card(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxSize(),
                        shape = RoundedCornerShape(999.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.inversePrimary,
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Box(
                            modifier = modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = recomend,
                                style = TextStyle(textAlign = TextAlign.Center),
                                autoSize = TextAutoSize.StepBased(),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onSaveFavorite,
                modifier = modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp, 8.dp, 8.dp, 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.save_station_button),
                    style = TextStyle(textAlign = TextAlign.Center),
                    autoSize = TextAutoSize.StepBased(),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    favorites: List<FavoriteStation>,
    selectedIds: List<Int>,
    isSelectionMode: Boolean,
    onCardClick: (FavoriteStation) -> Unit,
    onCardLongClick: (FavoriteStation) -> Unit,
    onDeleteSelected: () -> Unit,
    onOpenLocation: (FavoriteStation) -> Unit,
    modifier: Modifier = Modifier
) {
    if (favorites.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(0.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.empty_favorites_message),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            if (isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.selection_count,
                            selectedIds.size
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onDeleteSelected) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_selected)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp, 28.dp, 8.dp, 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites, key = { it.id }) { favorite ->
                    FavoriteStationCard(
                        favorite = favorite,
                        selected = favorite.id in selectedIds,
                        onClick = { onCardClick(favorite) },
                        onLongClick = { onCardLongClick(favorite) },
                        onOpenLocation = { onOpenLocation(favorite) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteStationCard(
    favorite: FavoriteStation,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onOpenLocation: (FavoriteStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val percentFormat = NumberFormat.getPercentInstance(Locale.getDefault())

    val dateFormatter = remember {
        DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
            Locale.getDefault()
        )
    }
    val createdAtText = remember(favorite.createdAt) {
        dateFormatter.format(Date(favorite.createdAt))
    }

    val colors = if (selected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    } else {
        CardDefaults.cardColors()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = colors
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = favorite.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = stringResource(R.string.favorite_created_at, createdAtText),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        R.string.favorite_ethanol_price,
                        currencyFormat.format(favorite.ethanolPrice)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.favorite_gasoline_price,
                        currencyFormat.format(favorite.gasolinePrice)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        R.string.favorite_ratio,
                        percentFormat.format(favorite.ratio)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.favorite_efficiency,
                        percentFormat.format(favorite.efficiency)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(999.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.inversePrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            R.string.favorite_recommendation,
                            favorite.recommendation
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (favorite.latitude != null && favorite.longitude != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilledTonalButton(
                        onClick = { onOpenLocation(favorite) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.open_location),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.location),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}


private const val PREFS_NAME = "etano_ou_gasolina_prefs"
private const val PREF_KEY_FAVORITES = "favorites_json"
private const val PREF_KEY_EFFICIENCY = "efficiency"
