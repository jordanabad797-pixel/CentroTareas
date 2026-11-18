package com.tecsup.centrotareas.data

import com.tecsup.centrotareas.model.AvancePrimos
import kotlinx.coroutines.flow.Flow

interface TareasRepository {
    fun tareaConProgreso(): Flow<Float>
    suspend fun contarPrimos(hasta: Int): Int
    fun contarPrimosConProgreso(hasta: Int): Flow<AvancePrimos>
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>
}
