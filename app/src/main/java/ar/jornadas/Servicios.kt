package ar.jornadas

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Room
import ar.jornadas.data.*
import ar.jornadas.lab.ServicioLocal
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Servicios private constructor(context: Context) {
    companion object {
        private var instancia: Servicios? = null

        @Synchronized
        fun obtener(context: Context): Servicios =
            instancia ?: Servicios(context.applicationContext).also { instancia = it }
    }

    val mesa = ServicioLocal(context)
    private val archivo = Room.databaseBuilder(context, Archivo::class.java, "cartelera.db")
        .addMigrations(object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE edicion (id INTEGER NOT NULL PRIMARY KEY)")
                db.execSQL("INSERT INTO edicion SELECT 1 WHERE EXISTS (SELECT 1 FROM charlas)")
            }
        }).build()
    private val api = Retrofit.Builder().baseUrl("https://programa.invalid/")
        .client(OkHttpClient.Builder().addInterceptor(mesa).build())
        .addConverterFactory(GsonConverterFactory.create()).build().create(ProgramaApi::class.java)
    val agenda = AgendaRepository(api, archivo.cartelera())
}
