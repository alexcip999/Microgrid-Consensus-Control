package infra.service

import domain.service.EncryptService
import org.mindrot.jbcrypt.BCrypt

class PasswordServiceImpl : EncryptService {
    override fun hash(plain: String): String =
        BCrypt.hashpw(plain, BCrypt.gensalt(12))

    override fun verify(plain: String, hashed: String): Boolean =
        BCrypt.checkpw(plain, hashed)
}