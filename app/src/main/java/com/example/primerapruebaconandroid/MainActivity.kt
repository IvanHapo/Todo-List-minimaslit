package com.example.primerapruebaconandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.primerapruebaconandroid.ui.theme.DarkGray
import com.example.primerapruebaconandroid.ui.theme.LightGray
import com.example.primerapruebaconandroid.ui.theme.PrimeraPruebaConAndroidTheme
import com.example.primerapruebaconandroid.ui.theme.White
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrimeraPruebaConAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoApp(
                        name = "Todolist", modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Room
@Entity(tableName = "tarea")
data class Tarea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, val texto: String, val completada: Boolean
)

@Dao
interface TareaDao {
    @Insert
    suspend fun insertar(tarea: Tarea)

    @Delete
    suspend fun eliminar(tarea: Tarea)

    @Update
    suspend fun actualizarTarea(tarea: Tarea)

    @Query("SELECT * FROM tarea")
    fun obtenerTodasTareas(): Flow<List<Tarea>>
}

@Database(entities = [Tarea::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tareaDao(): TareaDao
}

class TareaViewModel(private val dao: TareaDao) : ViewModel() {

    val tareas: Flow<List<Tarea>> = dao.obtenerTodasTareas()


    fun agregarTarea(texto: String) {
        viewModelScope.launch {
            val nuevaTarea = Tarea(texto = texto, completada = false)
            dao.insertar(nuevaTarea)
        }
    }

    fun eliminarTarea(tarea: Tarea) {
        viewModelScope.launch {
            dao.eliminar(tarea)
        }
    }

    fun actualizarTarea(tarea: Tarea) {
        viewModelScope.launch {
            dao.actualizarTarea(tarea)
        }
    }

}

// Funcion Tarea
@Composable
fun TodoApp(name: String, modifier: Modifier = Modifier) {
    var textoNuevo by remember { mutableStateOf("") }
    var mostrarDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val database = remember {
        Room.databaseBuilder(
            context, AppDatabase::class.java, "tarea_database"
        ).build()
    }
    val viewModel = remember {
        TareaViewModel(database.tareaDao())
    }
    val listaTareas by viewModel.tareas.collectAsState(initial = emptyList())

    Box(modifier = modifier.fillMaxSize()) {  // Box contenedor
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Todolist",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(16.dp)
            )

            // Lista de tareas
            for (tarea in listaTareas) {
                Card (modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkGray
                    ),
                    shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            tarea.texto,
                            color = if (tarea.completada) Color.Gray else Color.White,
                            textDecoration = if (tarea.completada) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = tarea.completada, onCheckedChange = {
                                val tareaActualizada = tarea.copy(completada = !tarea.completada)
                                viewModel.actualizarTarea(tareaActualizada)

                            })
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            viewModel.eliminarTarea(tarea)

                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        FloatingActionButton(
            onClick = { mostrarDialog = true },
            containerColor = White,
            contentColor = LightGray,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, "Agregar")
        }
    }
    // Ventana emergente input tarea
    if (mostrarDialog) {
        AlertDialog(
            onDismissRequest = { mostrarDialog = false },
            title = { Text("Nueva Tarea") },
            text = {
                TextField(
                    value = textoNuevo,
                    onValueChange = { textoNuevo = it },
                    label = { Text("Ingrese su tarea") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (textoNuevo.isNotBlank()) {
                            viewModel.agregarTarea(textoNuevo.trim())
                            textoNuevo = ""
                            mostrarDialog = false
                        }
                    }) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialog = false
                    textoNuevo = ""
                }) {
                    Text("Cancelar")
                }
            })
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PrimeraPruebaConAndroidTheme {
        TodoApp("Android")
    }
}