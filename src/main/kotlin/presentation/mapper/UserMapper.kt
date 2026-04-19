package presentation.mapper

import domain.model.user.User
import presentation.dto.response.AuthResponse
import presentation.dto.response.UserResponse

fun User.toAuthResponse(token: String) = AuthResponse(
    token = token,
    userId = id.toString(),
    role = role.name
)

fun User.toUserResponse() = UserResponse(
    id = id.toString(),
    email = email,
    role = role.name,
    createdAt = createdAt.toString()
)