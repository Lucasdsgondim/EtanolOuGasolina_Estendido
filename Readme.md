## 1. Salvar e restaurar o estado da eficiência (70% / 75%)

Embora o enunciado fale em um *Switch* para alternar entre 70% e 75%, o projeto foi implementado com um componente mais rico visualmente (`SingleChoiceSegmentedButtonRow` com dois botões – ver Ponto Extra). A lógica de salvar e restaurar o “estado do switch” (isto é, qual eficiência está escolhida) foi completamente atendida usando `SharedPreferences`.

### Leitura do valor salvo

Em `MainActivity.kt`, o app cria/acessa o arquivo de `SharedPreferences` e lê a eficiência salva usando uma chave específica:

```kotlin
private const val PREFS_NAME = "etano_ou_gasolina_prefs"
private const val PREF_KEY_EFFICIENCY = "efficiency"

@Composable
fun EtanolOuGasolina_EstendidoApp() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var eficiencia by rememberSaveable {
        mutableFloatStateOf(
            prefs.getFloat(PREF_KEY_EFFICIENCY, 0.7f)
        )
    }
    // ...
}
```

- `PREFS_NAME` define o arquivo de `SharedPreferences` do app.
- `PREF_KEY_EFFICIENCY` é a chave usada para gravar/ler a eficiência.
- `rememberSaveable` + `mutableFloatStateOf` guardam o valor no estado Compose, enquanto `prefs.getFloat(...)` restaura o valor persistido (padrão 0.7f = 70%).

Assim, quando o usuário volta ao aplicativo, o valor de eficiência carregado já corresponde à última escolha (70% ou 75%).

### Persistência automática da escolha

Sempre que o usuário muda o valor de eficiência, um `LaunchedEffect` grava o valor atualizado em `SharedPreferences`:

```kotlin
LaunchedEffect(eficiencia) {
    prefs.edit {
        putFloat(PREF_KEY_EFFICIENCY, eficiencia)
    }
}
```

- O `LaunchedEffect(eficiencia)` é reexecutado toda vez que `eficiencia` muda.
- `prefs.edit { putFloat(...) }` garante persistência entre execuções do app.

### Controle visual da eficiência (equivalente ao “switch” do enunciado)

Na calculadora (tanto em `CalculadoraPortrait` quanto em `CalculadoraLandscape`) o app oferece duas opções de eficiência (70% e 75%) usando um `SingleChoiceSegmentedButtonRow`:

```kotlin
val opts = listOf(0.7f, 0.75f)

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
```

- `opts` contém exatamente as duas eficiências pedidas: 0.7 (70%) e 0.75 (75%).
- A seleção altera `eficiencia` via `onEficienciaChange`, que em `EtanolOuGasolina_EstendidoApp()` faz `eficiencia = it` – e isso dispara o `LaunchedEffect` que salva no `SharedPreferences`.

Há ainda um *Switch* simples usado no diálogo de edição de um posto para alterar a eficiência local daquele registro:

```kotlin
Switch(
    checked = localEfficiency >= 0.75f,
    onCheckedChange = { checked ->
        localEfficiency = if (checked) 0.75f else 0.7f
    }
)
```

Esse controle também respeita o mesmo critério binário 70%/75%, mas aplicado apenas ao posto em edição; a eficiência global da calculadora continua sendo controlada pelos botões segmentados.

---

## 2. CRUD de valores de combustível com SharedPreferences

O requisito de criar, ler, atualizar e excluir (CRUD) valores de combustível de postos foi atendido com:
- um modelo serializável (`FavoriteStation`);
- armazenamento da lista em `SharedPreferences` usando JSON (`kotlinx.serialization`);
- telas para criar, editar e excluir registros.

### Modelo serializável e permissão de localização

Em `FavoriteStation.kt`:

```kotlin
@Serializable
data class FavoriteStation(
    val id: Int,
    val name: String,
    val ethanolPrice: Double,
    val gasolinePrice: Double,
    val efficiency: Float,
    val ratio: Double,
    val recommendation: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)

const val FINE_LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"
```

- `@Serializable` permite que a lista de `FavoriteStation` seja convertida para JSON e armazenada em `SharedPreferences`.
- O modelo inclui `name`, preços, eficiência, razão, recomendação, localização (`latitude`, `longitude`) e data de criação (`createdAt`).

### Leitura da lista de postos (R – Read)

Logo no início do `EtanolOuGasolina_EstendidoApp`, os favoritos são carregados de `SharedPreferences`:

```kotlin
var favorites by remember {
    mutableStateOf(
        prefs.getString(PREF_KEY_FAVORITES, null)?.let { json ->
            runCatching {
                Json.decodeFromString<List<FavoriteStation>>(json)
            }.getOrElse { emptyList() }
        } ?: emptyList()
    )
}
```

- `PREF_KEY_FAVORITES` é a chave para o JSON da lista de postos.
- Se houver dados gravados, o JSON é desserializado para `List<FavoriteStation>`.
- Em caso de erro de parse, o app se protege com `runCatching { ... }.getOrElse { emptyList() }`.

### Escrita/persistência da lista (C/U/D – Create/Update/Delete)

Qualquer mudança em `favorites` (tanto criação, quanto edição ou exclusão) dispara este `LaunchedEffect`:

```kotlin
LaunchedEffect(favorites) {
    val json = Json.encodeToString(favorites)
    prefs.edit {
        putString(PREF_KEY_FAVORITES, json)
    }
}
```

- `Json.encodeToString(favorites)` converte a lista de postos atualizada em JSON.
- `prefs.edit { putString(...) }` grava o JSON em `SharedPreferences`.
- Como `LaunchedEffect` observa `favorites`, não é necessário chamar manualmente nenhum método de “salvar”; qualquer alteração em memória é automaticamente persistida.

### Criação de um novo posto (C – Create)

Quando o usuário toca em “Salvar posto” na calculadora (depois de digitar valores válidos), o fluxo é:

1. `onSaveFavorite` verifica se há valores válidos e trata a permissão de localização;
2. abre o `AlertDialog` para digitar o nome do posto;
3. no botão Confirmar, cria um novo `FavoriteStation` e adiciona à lista.

Trecho principal:

```kotlin
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
    // ...
}
```

E dentro do `AlertDialog` de salvamento:

```kotlin
if (canSave) {
    val nextId = (favorites.maxOfOrNull { it.id } ?: 0) + 1
    val finalName = stationNameInput.ifBlank {
        context.getString(R.string.station_default_name, nextId)
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
```

- O app gera um `id` incremental.
- Associa nome, preços, eficiência, recomendação e localização atual (se disponível).
- Limpa os campos da calculadora após salvar.

### Edição de um posto existente (U – Update)

Quando o usuário toca em um card de posto na lista (modo normal, não seleção múltipla), o app entra no modo de edição:

```kotlin
fun handleFavoriteClick(station: FavoriteStation) {
    if (isSelectionMode) {
        toggleFavoriteSelection(station)
    } else {
        editingFavorite = station
    }
}
```

Isso abre um `AlertDialog` com campos para editar nome, valores e eficiência:

```kotlin
if (editingFavorite != null) {
    val station = editingFavorite!!

    var name by remember(station.id) { mutableStateOf(station.name) }
    var etanolText by remember(station.id) {
        mutableStateOf(((station.ethanolPrice * 100).toInt()).toString())
    }
    var gasolinaText by remember(station.id) {
        mutableStateOf(((station.gasolinePrice * 100).toInt()).toString())
    }
    var localEfficiency by remember(station.id) {
        mutableFloatStateOf(station.efficiency)
    }
    // ...
}
```

Nos campos de valor, a mesma lógica de “apenas números + formatação de moeda” é reutilizada (ver Ponto Extra), e a eficiência é alterada com um `Switch` 70%/75%.

No botão de salvar do diálogo de edição:

```kotlin
val etanol = etanolText.toLongOrNull()?.div(100.0)
val gasolina = gasolinaText.toLongOrNull()?.div(100.0)

if (etanol != null && gasolina != null && gasolina > 0.0) {
    val newRatio = etanol / gasolina
    val newRecommendation =
        if (newRatio <= localEfficiency) {
            context.getString(R.string.recommendation_ethanol)
        } else {
            context.getString(R.string.recommendation_gasoline)
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
```

- O registro é atualizado com novos valores, eficiência e recomendação recalculada.
- Em seguida, `favorites` é modificado e o `LaunchedEffect(favorites)` cuida de persistir em JSON.

### Exclusão de postos (D – Delete)

O app implementa exclusão *múltipla* com seleção de cards:

```kotlin
var selectedFavoriteIds by remember { mutableStateOf(listOf<Int>()) }

val isSelectionMode = selectedFavoriteIds.isNotEmpty()

fun toggleFavoriteSelection(station: FavoriteStation) {
    val id = station.id
    selectedFavoriteIds = if (id in selectedFavoriteIds) {
        selectedFavoriteIds - id
    } else {
        selectedFavoriteIds + id
    }
}

fun deleteSelectedFavorites() {
    if (selectedFavoriteIds.isNotEmpty()) {
        val idsToDelete = selectedFavoriteIds.toSet()
        favorites = favorites.filterNot { it.id in idsToDelete }
        selectedFavoriteIds = emptyList()
    }
}
```

Na lista (`FavoritesScreen`), um “long press” em um card ativa o modo de seleção, e a barra superior mostra quantos estão selecionados e um botão de lixeira:

```kotlin
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
            )
        )
        IconButton(onClick = onDeleteSelected) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_selected)
            )
        }
    }
}
```

- `onDeleteSelected` chama `deleteSelectedFavorites()`, que remove todos os selecionados.
- Como a lista `favorites` é regravada em `SharedPreferences`, a exclusão é persistente.

---

## 3. Exibição da lista de postos de combustível

O app possui uma tela específica para a lista de postos salvos, acessada via navegação inferior (`NavigationSuiteScaffold`) pela aba “Postos Salvos” (`AppDestinations.FAVORITES`).

### Navegação para a tela de favoritos

Em `EtanolOuGasolina_EstendidoApp()`:

```kotlin
enum class AppDestinations(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    CALCULATOR(R.string.dest_calculator, Icons.Default.Calculate),
    FAVORITES(R.string.dest_favorites, Icons.Default.LocalGasStation),
}
```

E no `NavigationSuiteScaffold`:

```kotlin
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
    // ...
    when (currentDestination) {
        AppDestinations.CALCULATOR -> { /* ... */ }
        AppDestinations.FAVORITES -> {
            FavoritesScreen(
                favorites = favorites,
                selectedIds = selectedFavoriteIds,
                isSelectionMode = isSelectionMode,
                onCardClick = { station -> handleFavoriteClick(station) },
                onCardLongClick = { station -> handleFavoriteLongClick(station) },
                onDeleteSelected = { deleteSelectedFavorites() },
                onOpenLocation = { station -> /* Intent para mapas */ }
            )
        }
    }
}
```

### Lista e estrutura visual dos cards

Na `FavoritesScreen`, a lista é exibida com `LazyColumn`, e cada posto é um `FavoriteStationCard`:

```kotlin
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
        // mensagem vazia
    } else {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            if (isSelectionMode) { /* barra de seleção + apagar */ }

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
```

O card em si exibe nome, preços, razão, eficiência, recomendação e data de cadastro:

```kotlin
@Composable
fun FavoriteStationCard(
    favorite: FavoriteStation,
    // ...
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        // ...
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
            // preços, razão, eficiência, recomendação...
        }
    }
}
```

- Os preços são formatados com `NumberFormat.getCurrencyInstance(Locale.getDefault())`.
- Razão e eficiência são exibidas com percentuais (`NumberFormat.getPercentInstance`).
- A data de cadastro usa `DateFormat` com o locale do sistema.
- O uso de `combinedClickable` permite clique simples (editar) e clique longo (selecionar para exclusão).

Essa tela atende ao requisito de exibir:
- nome do posto;
- valor de cada combustível;
- informações adicionais (razão, eficiência, recomendação e data de cadastro);
- opção de alterar (edição) ou excluir (seleção + lixeira) os registros.

Não há limite explícito de 10 postos; o app permite lista livre (conforme alternativa sugerida no enunciado).

---

## 4. Acesso à localização do usuário e exibição do mapa

O app:
- pede permissão de localização em tempo de execução;
- busca a última localização conhecida;
- salva latitude/longitude junto com os dados do posto;
- abre um Intent de mapa ao clicar em “Abrir localização” no card do posto.

### Permissão e captura da última localização

Logo após criar o `fusedLocationClient`, o app controla o estado da permissão:

```kotlin
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
```

E ainda um `LaunchedEffect` para buscar localização assim que a permissão estiver concedida:

```kotlin
LaunchedEffect(hasLocationPermission) {
    if (hasLocationPermission) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lastLocation = location
            }
        }
    }
}
```

### Integração com o fluxo de salvamento do posto

Ao salvar um posto, a lógica checa primeiro se os valores são válidos, depois a permissão de localização:

```kotlin
val onSaveFavorite: () -> Unit = onSaveFavorite@{
    if (!canSave) {
        // mostra Toast de erro
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
```

Quando o usuário confirma o diálogo de salvar, a localização é associada ao registro:

```kotlin
favorites = favorites + FavoriteStation(
    // ...
    latitude = lastLocation?.latitude,
    longitude = lastLocation?.longitude,
    createdAt = System.currentTimeMillis()
)
```

### Exibição do mapa ao clicar no posto

No card do posto, só é exibido o botão de mapa se latitude/longitude estiverem presentes:

```kotlin
if (favorite.latitude != null && favorite.longitude != null) {
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
```

E em `EtanolOuGasolina_EstendidoApp`, o callback `onOpenLocation` constrói o `Intent` usando o esquema `geo:`:

```kotlin
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
```

- Se não houver localização, o usuário é informado com um `Toast`.
- Se houver, o Intent abre o app de mapas padrão (Google Maps ou similar), focando nas coordenadas do posto.

---

## 5. Suporte a internacionalização (pt / en)

O aplicativo oferece suporte nativo a pelo menos duas línguas – português e inglês – e usa `stringResource` em toda a UI, seguindo o idioma definido no sistema operacional.

### Arquivos de recursos de texto

Português (`app/src/main/res/values/strings.xml`):

```xml
<resources>
    <string name="app_name">Etanol ou Gasolina?</string>
    <string name="dest_calculator">Calculadora</string>
    <string name="dest_favorites">Postos Salvos</string>
    <string name="recommendation_ethanol">Etanol</string>
    <string name="recommendation_gasoline">Gasolina</string>
    <!-- ... demais textos em pt-BR ... -->
</resources>
```

Inglês (`app/src/main/res/values-en/strings.xml`):

```xml
<resources>
    <string name="app_name">Ethanol or Gas?</string>
    <string name="dest_calculator">Calculator</string>
    <string name="dest_favorites">Favorite Stations</string>
    <string name="recommendation_ethanol">Ethanol</string>
    <string name="recommendation_gasoline">Gasoline</string>
    <!-- ... mesmos IDs com textos em inglês ... -->
</resources>
```

- Os mesmos IDs de string são definidos nas duas línguas.
- O Android carrega automaticamente a pasta `values` apropriada (`values/` ou `values-en/`) de acordo com o idioma do sistema.

### Uso consistente de `stringResource`

Na UI em Compose, o texto exibido é sempre obtido via `stringResource`, por exemplo:

```kotlin
Text(
    text = stringResource(R.string.calculator_title),
    // ...
)

OutlinedTextField(
    label = { Text(stringResource(R.string.ethanol_value_label)) },
    // ...
)
```

Essa abordagem garante:
- tradução correta conforme o locale do sistema;
- nenhum texto “hard-coded” em português ou inglês diretamente dentro do código Kotlin.

### Internacionalização além de textos

O app também respeita o locale para:

- Formatação de moeda:

```kotlin
val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
```

- Formatação de percentuais:

```kotlin
val formatPercent = NumberFormat.getPercentInstance(Locale.getDefault())
```

- Formatação de datas:

```kotlin
DateFormat.getDateTimeInstance(
    DateFormat.SHORT,
    DateFormat.SHORT,
    Locale.getDefault()
)
```

- Formatação de entrada de moeda (vírgula vs ponto) no `CurrencyVisualTransformation`, usando `DecimalFormatSymbols.getInstance(locale).decimalSeparator`.

Isso reforça a internacionalização não só nos textos, mas também na forma como valores numéricos e datas são apresentados.

---

## 6. Ponto extra – Funcionalidades além do enunciado

Além dos requisitos básicos, o projeto implementa várias melhorias de UX e de código. Duas delas foram mencionadas explicitamente no enunciado do exercício: a formatação automática dos valores de combustível e o uso de `SingleChoiceSegmentedButtonRow`. Há ainda outras melhorias interessantes.

### 6.1. Formatação automática dos valores de combustível

Para que o usuário não precise se preocupar com vírgulas ou pontos ao digitar os preços, foi criada a classe `CurrencyVisualTransformation`, que transforma a string digitada em um valor monetário formatado conforme o locale:

```kotlin
class CurrencyVisualTransformation(
    locale: Locale = Locale.getDefault()
) : VisualTransformation {

    private val decimalSeparator =
        DecimalFormatSymbols.getInstance(locale).decimalSeparator

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }

        val number = digits.toLongOrNull() ?: 0L
        val cents = (number % 100).toInt()
        val units = number / 100

        val formatted = if (digits.isEmpty()) {
            "0${decimalSeparator}00"
        } else if (units > 0) {
            "%d$decimalSeparator%02d".format(units, cents)
        } else {
            "0$decimalSeparator%02d".format(cents)
        }

        val transformed = AnnotatedString(formatted)
        val originalLength = digits.length
        val transformedLength = transformed.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = transformedLength
            override fun transformedToOriginal(offset: Int) = originalLength
        }

        return TransformedText(transformed, offsetMapping)
    }
}
```

Na prática, o usuário digita apenas números (ex.: `349`), e o campo mostra automaticamente `3,49` ou `3.49` dependendo do locale.

Na calculadora, os campos de valor de Etanol e Gasolina usam essa transformação e filtram a entrada para apenas dígitos:

```kotlin
OutlinedTextField(
    value = valoretanol,
    onValueChange = { newText ->
        onValoretanolChange(newText.filter { it.isDigit() })
    },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    visualTransformation = CurrencyVisualTransformation()
)
```

```kotlin
OutlinedTextField(
    value = valorgasolina,
    onValueChange = { newText ->
        onValorgasolinaChange(newText.filter { it.isDigit() })
    },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    visualTransformation = CurrencyVisualTransformation()
)
```

O mesmo padrão é reutilizado no diálogo de edição de postos:

```kotlin
OutlinedTextField(
    value = etanolText,
    onValueChange = { newText ->
        etanolText = newText.filter { it.isDigit() }
    },
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number
    ),
    visualTransformation = CurrencyVisualTransformation(),
    // ...
)
```

Isso melhora muito a experiência do usuário e reduz erros de digitação.

### 6.2. Uso de `SingleChoiceSegmentedButtonRow` no lugar de um Switch simples

Em vez de um switch simples para escolher 70% ou 75%, o app adota `SingleChoiceSegmentedButtonRow` com dois botões:

```kotlin
val opts = listOf(0.7f, 0.75f)

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
```

Vantagens dessa abordagem:
- visual mais moderno e claro (dois “chips” 70% / 75%);
- facilita adicionar novas opções de eficiência no futuro;
- integra-se bem com o restante da UI baseada em Material 3.

No diálogo de edição de um posto, um `Switch` é usado apenas para a eficiência local daquele registro (70%/75%), preservando o espírito do requisito original e, ao mesmo tempo, enriquecendo a interface principal.

### 6.3. Outras melhorias notáveis

Além dos pontos pedidos, o projeto traz:

- **Layout responsivo para orientação**  
  A calculadora é dividida em `CalculadoraPortrait` e `CalculadoraLandscape`, escolhida com base em `configuration.orientation`. Isso otimiza o uso de espaço em telas horizontais.

- **Navegação com `NavigationSuiteScaffold`**  
  Em vez de uma Activity com múltiplas telas estáticas, o app usa uma navegação de duas abas (Calculadora / Postos Salvos) com ícones e labels configurados, aproximando a UX de apps reais de produção.

- **Modo de seleção múltipla com “long press”**  
  O usuário pode selecionar vários postos com toque longo e apagar todos de uma vez, uma funcionalidade típica de listas avançadas.

- **Armazenamento da data de criação (`createdAt`) de cada posto**  
  Exibir quando o posto foi adicionado (em texto localizado) facilita a compreensão de quão “atual” é aquela informação.

- **Tratamento de erros com mensagens amigáveis via Toast**  
  Exemplos: tentar salvar sem valores, localização indisponível, erro ao abrir app de mapas, valores inválidos na edição.

---
## 0. Configurações de Gradle e Manifest necessárias

Antes das funcionalidades descritas nas seções seguintes funcionarem (SharedPreferences com JSON, localização, navegação adaptativa etc.), é preciso garantir algumas configurações no Gradle e no `AndroidManifest`.

### 0.1. Plugins e dependências do módulo `app`

No arquivo `app/build.gradle.kts`, além dos plugins padrão de Android/Compose, o projeto utiliza:

```kotlin
plugins {
    // ...
    alias(libs.plugins.kotlinSerialization) // para @Serializable e JSON
}
```

E, nas dependências, os pontos mais importantes para as soluções descritas no README são:

```kotlin
dependencies {
    // Compose + Material 3 (UI, NavigationSuite, ícones estendidos)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.foundation)

    // Serialização em JSON dos favoritos (SharedPreferences)
    implementation(libs.kotlinx.serialization.json)

    // Localização (FusedLocationProviderClient)
    implementation(libs.play.services.location)

    // Integração de ViewModel com Compose, quando necessário
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
```

Sem esses itens, o código mostrado nas seções de CRUD com JSON, localização e navegação adaptativa não compila.

### 0.2. Permissões e configurações do `AndroidManifest.xml`

No arquivo `app/src/main/AndroidManifest.xml`, o projeto adiciona as permissões de localização usadas pela solução de “Acesso à localização do usuário e exibição do mapa”:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

Ainda no mesmo arquivo, a activity principal é registrada com o tema do app e o filtro de lançamento:

```xml
<application
    android:icon="@mipmap/ic_etanolvsgasolina"
    android:roundIcon="@mipmap/ic_etanolvsgasolina_round"
    android:label="@string/app_name"
    android:theme="@style/Theme.EtanolOuGasolina_Estendido">

    <activity
        android:name=".MainActivity"
        android:exported="true"
        android:theme="@style/Theme.EtanolOuGasolina_Estendido">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

Essas permissões e configurações são necessárias para que:
- o app consiga solicitar a localização atual do usuário e salvar latitude/longitude junto com o posto;
- a aplicação use o tema correto e seja exibida normalmente na grade de apps (ícone, rótulo, activity principal).

---

 
## 1. Salvar e restaurar o estado da eficiǦncia (70% / 75%)
