package com.tecsup.centrotareas.data

import android.util.Log
import com.tecsup.centrotareas.model.AvancePrimos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class TareasRepositoryLocal : TareasRepository {

    override fun tareaConProgreso(): Flow<Float> = flow {
        Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
        for (i in 1..100) {
            delay(40)
            emit(i / 100f)
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun cargarPerfil(): String {
        delay(2000)
        return "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(simularError: Boolean): List<String> {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        return listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }

    override suspend fun contarPrimos(hasta: Int): Int = withContext(Dispatchers.Default) {
        var cantidad = 0
        for (n in 2..hasta) {
            ensureActive()
            if (esPrimo(n)) cantidad++
        }
        cantidad
    }

    override fun contarPrimosConProgreso(hasta: Int): Flow<AvancePrimos> = flow {
        var cantidad = 0
        val paso = maxOf(1, hasta / 100)
        for (n in 2..hasta) {
            currentCoroutineContext().ensureActive()
            if (esPrimo(n)) cantidad++
            if (n % paso == 0) emit(AvancePrimos.Progreso(n.toFloat() / hasta))
        }
        emit(AvancePrimos.Resultado(cantidad))
    }.flowOn(Dispatchers.Default)

    private fun esPrimo(n: Int): Boolean {
        var d = 2
        while (d.toLong() * d <= n) {
            if (n % d == 0) return false
            d++
        }
        return true
    }
}
