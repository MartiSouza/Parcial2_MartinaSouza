package ar.jornadas.data

import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException

class AgendaRepository(private val api: ProgramaApi, private val dao: CarteleraDao) {
    val programa = dao.observar()

    val hayEdicion = dao.observarEdicion()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val enCurso = MutableStateFlow(false)
    private val fallo = MutableStateFlow(false)
    val cargando = enCurso.asStateFlow()
    val error = fallo.asStateFlow()
    private var inicioAutomatico = false

    fun iniciar() {
        if (inicioAutomatico) return
        inicioAutomatico = true
        recargar()
    }

    fun recargar() {
        if (!enCurso.compareAndSet(false, true)) return
        fallo.value = false
        scope.launch {
            try {
                sincronizar()
            } catch (e: IOException) {
                fallo.value = true
            } catch (e: HttpException) {
                fallo.value = true
            } catch (e: android.database.sqlite.SQLiteException) {
                fallo.value = true
            } finally {
                enCurso.value = false
            }
        }
    }

    private suspend fun sincronizar() {
        val fichas = api.consultar().map {
            Charla(it.id, it.title, it.track, it.day, it.time, it.room, it.speaker, it.summary)
        }
        dao.reemplazar(fichas)
    }

    suspend fun cambiarFavorita(id: String) = dao.alternar(id)
}
