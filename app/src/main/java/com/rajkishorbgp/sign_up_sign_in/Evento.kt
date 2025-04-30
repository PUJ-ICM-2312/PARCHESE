import java.util.UUID

data class Evento(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val hora: String,
    val imagenUri: String,
    val ubicacion: String
)