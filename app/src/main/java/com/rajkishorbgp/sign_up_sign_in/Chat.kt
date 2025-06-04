import java.util.UUID

data class Chat(
    val usuarios: List<String> = emptyList(),
    val ultimoMensaje: String = "",
    val timestamp: Long = 0L
)
