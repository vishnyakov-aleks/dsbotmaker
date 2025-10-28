package studio.alot.dsbotmaker

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

/**
 * Пример использования StepDataHolder для демонстрации работы с данными шага
 */
class ExampleStepWithData : TelegramBotStep, TelegramBotStep.ButtonsSupported,
    TelegramBotStep.StepDataHolder<ExampleStepData> {

    override val dataStorage = mutableMapOf<Long, ExampleStepData>()

    override fun createStepData(userChatId: Long): ExampleStepData {
        return ExampleStepData().apply {
            // Инициализируем данные при первом создании
            userId = userChatId
            userName = "User_$userChatId" // В реальном приложении здесь был бы запрос к БД
            visitCount = 1
            lastVisit = System.currentTimeMillis()
        }
    }

    override val nextStepVariantTypes: Set<String> = setOf("next_step")

    override fun getType(): String = "example_step_with_data"

    override fun getBody(userChatId: Long): String {
        val data = getStepData(userChatId)
        return """
            📊 <b>Пример шага с данными</b>
            
            👤 Пользователь: ${data.userName}
            🆔 ID: ${data.userId}
            🔢 Количество посещений: ${data.visitCount}
            ⏰ Последнее посещение: ${java.time.Instant.ofEpochMilli(data.lastVisit)}
            
            Данные загружены один раз и переиспользуются во всех методах шага!
        """.trimIndent()
    }

    override fun getButtons(userChatId: Long): List<KeyboardRow> {
        val data = getStepData(userChatId) // Используем уже загруженные данные
        
        return listOf(
            KeyboardRow().apply {
                add("➕ Увеличить счетчик (${data.visitCount})")
            },
            KeyboardRow().apply {
                add("🔄 Обновить имя")
            },
            KeyboardRow().apply {
                add("➡️ Следующий шаг")
            }
        )
    }

    override fun getInputPlaceholder(userChatId: Long): String? = "Введите новое имя"

    // Пример метода для обновления данных
    fun incrementCounter(userChatId: Long) {
        updateStepData(userChatId) {
            visitCount++
            lastVisit = System.currentTimeMillis()
        }
    }

    // Пример метода для изменения имени
    fun updateUserName(userChatId: Long, newName: String) {
        updateStepData(userChatId) {
            userName = newName
            lastVisit = System.currentTimeMillis()
        }
    }
}

/**
 * Пример класса данных для шага
 * Каждый шаг может иметь свой собственный класс данных
 */
data class ExampleStepData(
    var userId: Long = 0,
    var userName: String = "",
    var visitCount: Int = 0,
    var lastVisit: Long = 0
)
