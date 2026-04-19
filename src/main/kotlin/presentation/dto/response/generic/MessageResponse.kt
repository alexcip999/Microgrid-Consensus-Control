package presentation.dto.response.generic

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val message: String
)