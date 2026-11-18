package com.tecsup.centrotareas.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.centrotareas.data.TareasRepository
import com.tecsup.centrotareas.model.AvancePrimos
import com.tecsup.centrotareas.model.CargaUi
import com.tecsup.centrotareas.model.PrimosUi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class TareasViewModel(private val repo: TareasRepository) : ViewModel() {

    private val _mensaje = MutableStateFlow("Listo")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso.asStateFlow()

    private val _corriendo = MutableStateFlow(false)
    val corriendo: StateFlow<Boolean> = _corriendo.asStateFlow()

    private var trabajo: Job? = null

    fun tareaLarga() {
        if (_corriendo.value) return
        _corriendo.value = true
        trabajo = viewModelScope.launch {
            try {
                _progreso.value = 0f
                _mensaje.value = "Trabajando..."
                Log.d("HILO", "Antes: ${Thread.currentThread().name}")
                repo.tareaConProgreso().collect { valor ->
                    _progreso.value = valor
                }
                Log.d("HILO", "Despues: ${Thread.currentThread().name}")
                _mensaje.value = "Terminó"
            } finally {
                _corriendo.value = false
            }
        }
    }

    fun cancelar() {
        trabajo?.cancel()
        _mensaje.value = "Cancelado"
    }

    private val _primos = MutableStateFlow<PrimosUi>(PrimosUi.Inicial)
    val primos: StateFlow<PrimosUi> = _primos.asStateFlow()

    private var trabajoPrimos: Job? = null

    fun contar(hasta: Int) {
        if (_primos.value is PrimosUi.Calculando) return
        _primos.value = PrimosUi.Calculando(0f)
        trabajoPrimos = viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            try {
                repo.contarPrimosConProgreso(hasta).collect { avance ->
                    when (avance) {
                        is AvancePrimos.Progreso -> _primos.value = PrimosUi.Calculando(avance.fraccion)
                        is AvancePrimos.Resultado -> _primos.value = PrimosUi.Listo(
                            cantidad = avance.cantidad,
                            milisegundos = System.currentTimeMillis() - inicio
                        )
                    }
                }
            } catch (e: CancellationException) {
                _primos.value = PrimosUi.Cancelado
                throw e
            }
        }
    }

    fun cancelarPrimos() {
        trabajoPrimos?.cancel()
    }

    private val _carga = MutableStateFlow<CargaUi>(CargaUi.Inicial)
    val carga: StateFlow<CargaUi> = _carga.asStateFlow()

    private val _simularError = MutableStateFlow(false)
    val simularError: StateFlow<Boolean> = _simularError.asStateFlow()

    fun cambiarSimularError(valor: Boolean) {
        _simularError.value = valor
    }

    private val _limiteTiempo = MutableStateFlow(false)
    val limiteTiempo: StateFlow<Boolean> = _limiteTiempo.asStateFlow()

    fun cambiarLimiteTiempo(valor: Boolean) {
        _limiteTiempo.value = valor
    }

    private suspend fun <T> conLimite(bloque: suspend () -> T): T =
        if (_limiteTiempo.value) withTimeout(2000) { bloque() } else bloque()

    private val _reintento = MutableStateFlow(0)
    val reintento: StateFlow<Int> = _reintento.asStateFlow()

    private suspend fun <T> conReintentos(bloque: suspend () -> T): T {
        var espera = 500L
        for (intento in 1..3) {
            try {
                return bloque()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _reintento.value = intento
                delay(espera)
                espera *= 2
            }
        }
        return bloque()
    }

    fun cargarUnaPorUna() {
        if (_carga.value is CargaUi.Cargando) return
        _carga.value = CargaUi.Cargando
        _reintento.value = 0
        viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            try {
                val (perfil, pedidos) = conLimite {
                    conReintentos {
                        val perfil = repo.cargarPerfil()
                        val pedidos = repo.cargarPedidos(_simularError.value)
                        Pair(perfil, pedidos)
                    }
                }
                _carga.value = CargaUi.Exito(
                    modo = "Una por una",
                    perfil = perfil,
                    pedidos = pedidos.size,
                    milisegundos = System.currentTimeMillis() - inicio
                )
            } catch (e: TimeoutCancellationException) {
                _carga.value = CargaUi.Error("Tardó demasiado")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _carga.value = CargaUi.Error("No se pudo cargar: " + e.message)
            } finally {
                _reintento.value = 0
            }
        }
    }

    fun cargarALaVez() {
        if (_carga.value is CargaUi.Cargando) return
        _carga.value = CargaUi.Cargando
        _reintento.value = 0
        viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            try {
                val (perfil, pedidos) = conLimite {
                    conReintentos {
                        coroutineScope {
                            val a = async { repo.cargarPerfil() }
                            val b = async { repo.cargarPedidos(_simularError.value) }
                            Pair(a.await(), b.await())
                        }
                    }
                }
                _carga.value = CargaUi.Exito(
                    modo = "A la vez",
                    perfil = perfil,
                    pedidos = pedidos.size,
                    milisegundos = System.currentTimeMillis() - inicio
                )
            } catch (e: TimeoutCancellationException) {
                _carga.value = CargaUi.Error("Tardó demasiado")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _carga.value = CargaUi.Error("No se pudo cargar: " + e.message)
            } finally {
                _reintento.value = 0
            }
        }
    }
}
