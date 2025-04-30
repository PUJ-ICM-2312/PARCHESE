import java.util.UUID

data class Chat(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lastMessage: String,
    val time: String,
    val profileImage: String = "",
    val unreadCount: Int = 0
)