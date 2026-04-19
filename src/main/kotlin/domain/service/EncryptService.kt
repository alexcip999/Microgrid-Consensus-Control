package domain.service

interface EncryptService {
    fun hash(plain: String): String
    fun verify(plain: String, hashed: String): Boolean
}