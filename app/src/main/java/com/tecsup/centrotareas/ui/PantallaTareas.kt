package com.tecsup.centrotareas.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecsup.centrotareas.model.CargaUi
import com.tecsup.centrotareas.model.PrimosUi
import com.tecsup.centrotareas.ui.theme.VerdeFondo
import com.tecsup.centrotareas.ui.theme.VerdeTexto
import com.tecsup.centrotareas.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaProgreso(vm)
            TarjetaPrimos(vm)
            TarjetaCargas(vm)
        }
    }
}

@Composable
private fun TarjetaProgreso(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsState()
    val progreso by vm.progreso.collectAsState()
    val corriendo by vm.corriendo.collectAsState()
    var toques by rememberSaveable { mutableIntStateOf(0) }
    Tarjeta(titulo = "Tarea con progreso") {
        Button(onClick = { toques++ }) {
            Text("Tócame: $toques", fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { vm.tareaLarga() }, enabled = !corriendo) {
                Text("Iniciar", fontWeight = FontWeight.Bold)
            }
            BotonContorno(texto = "Cancelar", habilitado = corriendo) { vm.cancelar() }
        }
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        Text(
            text = "${(progreso * 100).toInt()} %",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = mensaje, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun TarjetaPrimos(vm: TareasViewModel) {
    val primos by vm.primos.collectAsState()
    var texto by rememberSaveable { mutableStateOf("3000000") }
    var hastaContado by rememberSaveable { mutableIntStateOf(0) }
    val numero = texto.toIntOrNull()
    val valido = numero != null && numero >= 2
    val calculando = primos is PrimosUi.Calculando
    Tarjeta(titulo = "Contar números primos") {
        OutlinedTextField(
            value = texto,
            onValueChange = { nuevo -> texto = nuevo.filter { it.isDigit() } },
            label = { Text("Hasta el número") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    if (numero != null && numero >= 2) {
                        hastaContado = numero
                        vm.contar(numero)
                    }
                },
                enabled = valido && !calculando
            ) {
                Text("Contar", fontWeight = FontWeight.Bold)
            }
            BotonContorno(texto = "Cancelar", habilitado = calculando) { vm.cancelarPrimos() }
        }
        when (val estado = primos) {
            is PrimosUi.Inicial -> Unit
            is PrimosUi.Calculando -> Text(
                text = "Calculando...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            is PrimosUi.Cancelado -> Text(
                text = "Cancelado",
                color = MaterialTheme.colorScheme.onSurface
            )
            is PrimosUi.Listo -> Recuadro(fondo = VerdeFondo) {
                Text(
                    text = "Primos hasta $hastaContado: ${estado.cantidad}",
                    color = VerdeTexto,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Tiempo: ${estado.milisegundos} ms", color = VerdeTexto)
            }
        }
    }
}

@Composable
private fun TarjetaCargas(vm: TareasViewModel) {
    val carga by vm.carga.collectAsState()
    val simularError by vm.simularError.collectAsState()
    val cargando = carga is CargaUi.Cargando
    Tarjeta(titulo = "Dos cargas") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Switch(
                checked = simularError,
                onCheckedChange = { vm.cambiarSimularError(it) }
            )
            Text(text = "Simular error", color = MaterialTheme.colorScheme.onSurface)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotonContorno(texto = "Una por una", habilitado = !cargando) { vm.cargarUnaPorUna() }
            Button(onClick = { vm.cargarALaVez() }, enabled = !cargando) {
                Text("A la vez", fontWeight = FontWeight.Bold)
            }
        }
        when (val estado = carga) {
            is CargaUi.Inicial -> Unit
            is CargaUi.Cargando -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            is CargaUi.Exito -> Recuadro(fondo = VerdeFondo) {
                Text(
                    text = "Modo: ${estado.modo}",
                    color = VerdeTexto,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Perfil: ${estado.perfil}", color = VerdeTexto)
                Text(text = "Pedidos: ${estado.pedidos}", color = VerdeTexto)
                Text(text = "Tiempo: ${estado.milisegundos} ms", color = VerdeTexto)
            }
            is CargaUi.Error -> {
                Recuadro(fondo = MaterialTheme.colorScheme.errorContainer) {
                    Text(
                        text = estado.mensaje,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                BotonContorno(texto = "Reintentar", habilitado = true) { vm.cargarALaVez() }
            }
        }
    }
}

@Composable
private fun Tarjeta(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = titulo,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier.fillMaxWidth()
            )
            contenido()
        }
    }
}

@Composable
private fun Recuadro(fondo: Color, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(fondo, RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        content = contenido
    )
}

@Composable
private fun BotonContorno(texto: String, habilitado: Boolean, alPulsar: () -> Unit) {
    OutlinedButton(
        onClick = alPulsar,
        enabled = habilitado,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (habilitado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(texto, fontWeight = FontWeight.Bold)
    }
}
