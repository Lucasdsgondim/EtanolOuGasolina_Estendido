package com.example.etanoougasolina_extendido

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AdUnits
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
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
                    label = { Text(it.label) },
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
                verticalArrangement = Arrangement.Top)
            {
                Text(text = "Calculadora",
                    fontSize = 30.sp)
                Spacer(modifier=Modifier.height(12.dp))
                Text(text = "Informe os preços de litro dos combustíveis e a distância entre os postos",
                    fontSize = 16.sp)
                Spacer(modifier=Modifier.height(12.dp))
                Text(text = "Valor do Etanol",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold)
                Spacer(modifier=Modifier.height(4.dp))
                var valoretanol by rememberSaveable { mutableStateOf("") }
                OutlinedTextField(value = valoretanol, onValueChange = { valoretanol = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 8.dp, 8.dp, 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                var valorgasolina by rememberSaveable { mutableStateOf("") }
                Spacer(modifier=Modifier.height(4.dp))
                Text(text = "Valor da Gasolina",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 12.dp),
                    textAlign = TextAlign.Right)
                Spacer(modifier=Modifier.height(4.dp))
                OutlinedTextField(value = valorgasolina, onValueChange = { valorgasolina = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 8.dp, 8.dp, 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                var eficiencia by rememberSaveable { mutableStateOf(0.7f) }
                Spacer(modifier=Modifier.height(4.dp))
                val opts = listOf(0.7f, 0.75f)
                Text(text = "Critério de eficiência",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center)
                SingleChoiceSegmentedButtonRow (modifier=Modifier.fillMaxWidth()
                    .padding(8.dp, 8.dp, 8.dp, 8.dp)){
                    opts.forEachIndexed { i, v ->
                    SegmentedButton(
                        selected = (eficiencia == v),
                        onClick = { eficiencia = v },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = opts.size, baseShape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                    ) {
                        Text("${(v*100).toInt()}%")
                    }
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
            Text(text = "Calcular")
        }
        Card(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp, 8.dp, 8.dp, 8.dp)
            .weight(3f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {  }
    }
}