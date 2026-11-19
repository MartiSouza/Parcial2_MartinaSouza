package ar.jornadas.data

import androidx.room.*
import retrofit2.http.GET
import kotlinx.coroutines.flow.Flow

data class FichaRemota(val id: String, val title: String, val track: String, val day: Int,
    val time: String, val room: String, val speaker: String, val summary: String)

interface ProgramaApi {
    @GET("programa") suspend fun consultar(): List<FichaRemota>
}

@Entity(tableName = "charlas")
data class Charla(@PrimaryKey val id: String, val titulo: String, val tema: String, val dia: Int,
    val hora: String, val sala: String, val ponente: String, val resumen: String,
    val favorita: Boolean = false)

@Entity(tableName = "edicion")
data class Edicion(@PrimaryKey val id: Int = 1)

@Dao
interface CarteleraDao {
    @Query("SELECT * FROM charlas ORDER BY dia, hora, sala")
    fun observar(): Flow<List<Charla>>

    @Query("SELECT EXISTS(SELECT 1 FROM edicion)")
    fun observarEdicion(): Flow<Boolean>

    @Query("SELECT id FROM charlas WHERE favorita = 1")
    suspend fun favoritas(): List<String>

    @Query("DELETE FROM charlas")
    suspend fun vaciar()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registrar(edicion: Edicion)

    @Transaction
    suspend fun reemplazar(fichas: List<Charla>) {
        val marcadas = favoritas().toSet()
        vaciar()
        guardar(fichas.map { it.copy(favorita = it.id in marcadas) })
        registrar(Edicion())
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(fichas: List<Charla>)

    @Query("UPDATE charlas SET favorita = NOT favorita WHERE id = :id")
    suspend fun alternar(id: String)
}

@Database(entities = [Charla::class, Edicion::class], version = 2, exportSchema = false)
abstract class Archivo : RoomDatabase() {
    abstract fun cartelera(): CarteleraDao
}
