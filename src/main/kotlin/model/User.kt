package model

//TODO deserialize according to spec
data class User(
    @Transient val id: Long,
    val name: String
)

data class UsersResponse(val users: List<User>)
