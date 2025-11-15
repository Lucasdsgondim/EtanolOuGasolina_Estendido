package com.example.etanoougasolina_extendido

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
import androidx.compose.foundation.text.BasicText
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
import com.example.etanoougasolina_extendido.ui.theme.EtanoOuGasolina_ExtendidoTheme
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EtanoOuGasolina_ExtendidoTheme {
                EtanoOuGasolina_ExtendidoApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun EtanoOuGasolina_ExtendidoApp() {
    val context = LocalContext.current

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.CALCULATOR) }
    var valoretanol by rememberSaveable { mutableStateOf("") }
    var valorgasolina by rememberSaveable { mutableStateOf("") }
    var eficiencia by rememberSaveable { mutableFloatStateOf(0.7f) }
    var favorites by rememberSaveable { mutableStateOf(listOf<FavoriteStation>()) }

    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var stationNameInput by rememberSaveable { mutableStateOf("") }

    val opts = listOf(0.7f, 0.75f)

    val etanolDouble = valoretanol.replace(',', '.').toDoubleOrNull() ?: 0.0
    val gasolinaDouble = valorgasolina.replace(',', '.').toDoubleOrNull() ?: 0.0

    val razao = if (gasolinaDouble > 0) etanolDouble / gasolinaDouble else 0.0
    val recomend =
        if (gasolinaDouble > 0 && razao <= eficiencia) "Etanol" else "Gasolina"

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

    val canSave = etanolDouble > 0.0 && gasolinaDouble > 0.0

    val onSaveFavorite: () -> Unit = onSaveFavorite@{
        if (!canSave) {
            return@onSaveFavorite
        }

        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(FINE_LOCATION_PERMISSION)
        } else {
            val nextId = favorites.size + 1
            stationNameInput = "Posto $nextId"
            showSaveDialog = true
        }
    }

    val formatPercent = NumberFormat.getPercentInstance(Locale.getDefault())
    val razaoFormat = formatPercent.format(razao)
    val eficienciaFormat = formatPercent.format(eficiencia)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        },
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
                    eficienciaFormat = eficienciaFormat,
                    recomend = recomend,
                    onSaveFavorite = onSaveFavorite
                )
            }

            AppDestinations.FAVORITES -> {
                FavoritesScreen(
                    favorites = favorites,
                    onOpenLocation = { station ->

                        val lat = station.latitude
                        val lon = station.longitude

                        if (lat == null || lon == null) {
                            Toast.makeText(
                                context,
                                "Localização não disponível para este posto.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val uri =
                                "geo:$lat,$lon?q=$lat,$lon(${Uri.encode(station.name)})".toUri()
                            val intent = Intent(Intent.ACTION_VIEW, uri)

                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Não foi possível abrir o app de mapas.",
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
                title = { Text(text = "Salvar posto") },
                text = {
                    OutlinedTextField(
                        value = stationNameInput,
                        onValueChange = { stationNameInput = it },
                        singleLine = true,
                        label = { Text("Nome do posto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (canSave) {
                                val nextId = favorites.size + 1
                                val finalName =
                                    stationNameInput.ifBlank { "Posto $nextId" }

                                favorites = favorites + FavoriteStation(
                                    id = nextId,
                                    name = finalName,
                                    ethanolPrice = etanolDouble,
                                    gasolinePrice = gasolinaDouble,
                                    efficiency = eficiencia,
                                    ratio = razao,
                                    recommendation = recomend,
                                    latitude = lastLocation?.latitude,
                                    longitude = lastLocation?.longitude
                                )
                            }
                            stationNameInput = ""
                            showSaveDialog = false
                        }
                    ) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            stationNameInput = ""
                            showSaveDialog = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}



enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    CALCULATOR("Calculadora", Icons.Default.Calculate),
    FAVORITES("Postos Salvos", Icons.Default.LocalGasStation),
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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp, 14.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                Text(
                    text = "Calculadora",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(.625f),
                    autoSize = TextAutoSize.StepBased(),
                )
                Text(
                    text = "Informe os preços de litro dos combustíveis e a distância entre os postos",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Light),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.625f),
                    autoSize = TextAutoSize.StepBased()
                )
                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    text = "Valor do Etanol",
                    style = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.325f),
                    autoSize = TextAutoSize.StepBased()
                )

                OutlinedTextField(
                    value = valoretanol, onValueChange = { onValoretanolChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.15f),
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    label = { Text("Valor do Etanol") },
                    prefix = { Text("R$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.weight(.1f))
                Text(
                    text = "Valor da Gasolina",
                    style = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.325f),
                    autoSize = TextAutoSize.StepBased()
                )

                OutlinedTextField(
                    value = valorgasolina, onValueChange = { onValorgasolinaChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.15f),
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    label = { Text("Valor da Gasolina") },
                    prefix = { Text("R$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.weight(.1f))

                Text(
                    text = "Critério de eficiência",
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
                                baseShape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ),
                            modifier = Modifier.fillMaxHeight(fraction = 0.75f),
                        ) {
                            Text("${(v * 100).toInt()}%")
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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp, 14.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Resultado",
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
                        text = "Razão entre os valores:",
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
                        text = "Críterio de eficiência:",
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
                        text = "Recomendação:", style = TextStyle(textAlign = TextAlign.Left),
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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "Salvar Posto",
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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp, 14.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                BasicText(
                    text = "Calculadora",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(.625f),
                    autoSize = TextAutoSize.StepBased(),
                )
                Text(
                    text = "Informe os preços de litro dos combustíveis e a distância entre os postos",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Light),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.625f),
                    autoSize = TextAutoSize.StepBased()
                )
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.325f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically){
                    Text(
                        text = "Valor do Etanol:",
                        modifier = Modifier
                            .weight(2f),
                        textAlign = TextAlign.Left,
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(10.sp, 25.sp)
                    )



                    TextField(
                        value = valoretanol, onValueChange = { onValoretanolChange(it) },
                        modifier = Modifier
                            .weight(1.75f),
                        singleLine = true,
                        label = { Text("Valor do Etanol") },
                        prefix = { Text("R$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.325f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically){
                    Text(
                        text = "Valor da Gasolina:",
                        modifier = Modifier.weight(2f),
                        textAlign = TextAlign.Left,
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(10.sp, 25.sp)
                    )

                    TextField(
                        value = valorgasolina, onValueChange = { onValorgasolinaChange(it) },
                        modifier = Modifier
                            .weight(1.75f),
                        singleLine = true,
                        label = { Text("Valor da Gasolina") },
                        prefix = { Text("R$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.325f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically){
                    Text(
                        text = "Critério de eficiência:",
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
                                    baseShape = androidx.compose.foundation.shape.RoundedCornerShape(
                                        16.dp
                                    )
                                ),
                                modifier = Modifier.fillMaxHeight(fraction = 0.75f),
                            ) {
                                Text("${(v * 100).toInt()}%")
                            }
                        }
                    }
                }
            }
        }
        Column(modifier = Modifier
            .weight(1f)) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp, 28.dp, 8.dp, 2.dp)
                    .weight(3f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            )
            {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp, 14.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Resultado",
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
                            text = "Razão entre os valores:",
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
                            text = "Críterio de eficiência:",
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
                            text = "Recomendação:", style = TextStyle(textAlign = TextAlign.Left),
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
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Salvar Posto",
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
                text = "Nenhum posto salvo ainda.\nUse o botão \"Salvar Posto\" na calculadora.",
                textAlign = TextAlign.Center,
                modifier = modifier.fillMaxWidth()
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp , 28.dp, 8.dp , 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favorites, key = { it.id }) { favorite ->
                FavoriteStationCard(
                    favorite = favorite,
                    onOpenLocation = { onOpenLocation(favorite) }
                )
            }
        }
    }
}


@Composable
fun FavoriteStationCard(
    favorite: FavoriteStation,
    onOpenLocation: (FavoriteStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val percentFormat = NumberFormat.getPercentInstance(Locale.getDefault())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Etanol: ${currencyFormat.format(favorite.ethanolPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Gasolina: ${currencyFormat.format(favorite.gasolinePrice)}",
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
                    text = "Razão: ${percentFormat.format(favorite.ratio)}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Critério: ${percentFormat.format(favorite.efficiency)}",
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
                        text = "Recomendação: ${favorite.recommendation}",
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
                    androidx.compose.material3.FilledTonalButton(
                        onClick = { onOpenLocation(favorite) },
                        modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Abrir localização",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Localização",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
