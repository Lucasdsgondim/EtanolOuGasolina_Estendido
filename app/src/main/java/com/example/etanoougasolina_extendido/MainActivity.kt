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
import androidx.compose.foundation.layout.fitInside
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
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.example.etanoougasolina_extendido.ui.theme.EtanoOuGasolina_ExtendidoTheme

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
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.CALCULATOR) }

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
        }
    ) {
        when (currentDestination) {
        AppDestinations.CALCULATOR -> {
            Calculadora()
        }

        AppDestinations.FAVORITES -> {

        }
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
fun Calculadora(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        var valoretanol by rememberSaveable { mutableStateOf("") }
        var valorgasolina by rememberSaveable { mutableStateOf("") }
        var eficiencia by rememberSaveable { mutableFloatStateOf(0.7f) }
        val opts = listOf(0.7f, 0.75f)
        Card(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp, 28.dp, 8.dp, 8.dp)
            .weight(7f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
        {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 14.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp))
            {
                BasicText(
                    text = "Calculadora",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(.625f),
                    autoSize = TextAutoSize.StepBased(                    ),
                )
                BasicText(text = "Informe os preços de litro dos combustíveis e a distância entre os postos",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Light),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.625f),
                    autoSize = TextAutoSize.StepBased())
                Spacer(modifier = Modifier.weight(.1f))
                BasicText(text = "Valor do Etanol",
                    style = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.325f),
                    autoSize = TextAutoSize.StepBased()
                )

                OutlinedTextField(value = valoretanol, onValueChange = { valoretanol = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    label = { Text("Valor do Etanol") },
                    prefix = { Text("R$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Spacer(modifier = Modifier.weight(.1f))
                BasicText(text = "Valor da Gasolina",
                    style = TextStyle(fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.325f),
                    autoSize = TextAutoSize.StepBased())

                OutlinedTextField(value = valorgasolina, onValueChange = { valorgasolina = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    label = { Text("Valor da Gasolina") },
                    prefix = { Text("R$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.weight(.1f))

                BasicText(text = "Critério de eficiência",
                    style = TextStyle(
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.325f),
                    autoSize = TextAutoSize.StepBased(),
                    )
                SingleChoiceSegmentedButtonRow (modifier=Modifier
                    .weight(1f)
                    .fillMaxWidth()){
                    opts.forEachIndexed { i, v ->
                    SegmentedButton(
                        selected = (eficiencia == v),
                        onClick = { eficiencia = v },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = opts.size, baseShape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                            modifier=Modifier.fillMaxHeight(fraction = 0.75f),
                    ) {
                        Text("${(v*100).toInt()}%")
                    }
                    }
                }
            }
        }
        Card(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp, 8.dp, 8.dp, 8.dp)
            .weight(3f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
        {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 14.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(
                    text = "Resultado",
                    style = TextStyle(fontSize = 18.sp),
                    autoSize = TextAutoSize.StepBased(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.75f)
                )

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Razão entre os valores:",style = TextStyle(textAlign = TextAlign.Left, fontWeight = FontWeight.Light),
                        modifier = Modifier.weight(2f),
                        autoSize = TextAutoSize.StepBased(),

                        )
                    BasicText(text = "68%",style = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Light),
                        modifier = Modifier.weight(1f),
                        autoSize = TextAutoSize.StepBased(),

                        )
                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                    verticalAlignment = Alignment.CenterVertically) {
                    BasicText(text = "Críterio de eficiência:",style = TextStyle(textAlign = TextAlign.Left, fontWeight = FontWeight.Light),
                        modifier = Modifier.weight(2f),
                        autoSize = TextAutoSize.StepBased(),

                        )
                    BasicText(text = "70%",style = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Light),
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        autoSize = TextAutoSize.StepBased(),

                        )
                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.25f)) {
                    BasicText(text = "Recomendação:",style = TextStyle(textAlign = TextAlign.Left),
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

                ){
                    Box(modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center){
                        BasicText(
                            text = "Gasolina",
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
        Button(onClick = { /*TODO*/ },
            modifier = modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp, 8.dp, 8.dp, 8.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
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