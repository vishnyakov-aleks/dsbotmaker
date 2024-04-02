package studio.alot.avitowheelsparser.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional
@Repository
interface TgUserRepository : JpaRepository<TgUserEntity, Long> {

    @Query("select su.cookies from TgUserEntity su where su.userChatId=:userChatId")
    fun getCookies(userChatId: Long): String?

    @Modifying
    @Query("update TgUserEntity set cookies=:cookiesJson where userChatId=:userChatId")
    fun saveCookies(userChatId: Long, cookiesJson: String)

    @Query("select su.currentStep from TgUserEntity su where su.userChatId=:userChatId")
    fun getCurrentStep(userChatId: Long): Step?

    @Modifying
    @Query("update TgUserEntity set currentStep=:step where userChatId=:userChatId")
    fun updateStep(userChatId: Long, step: Step)
}