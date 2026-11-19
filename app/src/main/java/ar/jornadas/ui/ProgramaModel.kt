package ar.jornadas.ui

import android.app.Application
import androidx.lifecycle.*
import ar.jornadas.Servicios
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProgramaModel(app: Application, private val estado: SavedStateHandle) : AndroidViewModel(app) {
    private val servicios = Servicios.obtener(app)
    val mesa = servicios.mesa
    private val repo = servicios.agenda
    val charlas = repo.programa.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val dia = estado.getStateFlow("dia", 0)
    val tema = estado.getStateFlow("tema", "Todos")
    val hayEdicion = repo.hayEdicion.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val error = repo.error
    val cargando = repo.cargando

    fun elegirDia(dia: Int) { estado["dia"] = dia }
    fun elegirTema(tema: String) { estado["tema"] = tema }
    fun iniciar() = repo.iniciar()
    fun recargar() = repo.recargar()

    fun marcar(id: String) { viewModelScope.launch { repo.cambiarFavorita(id) } }
}
