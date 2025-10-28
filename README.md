# DSBotMaker - Фреймворк для создания Telegram ботов

## Содержание

- [Общая архитектура](#общая-архитектура)
- [Инстанс бота (DeepStateBotInstance)](#инстанс-бота-deepstatebotinstance)
  - [Создание инстанса](#создание-инстанса)
  - [Основные методы инстанса](#основные-методы-инстанса)
  - [Управление шагами](#управление-шагами)
  - [Работа с cookies (данными состояния)](#работа-с-cookies-данными-состояния)
  - [Отправка сообщений через TelegramBotSender](#отправка-сообщений-через-telegrambotsender)
  - [Практические примеры использования](#практические-примеры-использования)
  - [Обработка ошибок](#обработка-ошибок)
- [Ключевые интерфейсы и маркеры](#ключевые-интерфейсы-и-маркеры)
  - [Основные интерфейсы шагов (TelegramBotStep)](#основные-интерфейсы-шагов-telegrambotstep)
  - [Интерфейс хранения данных в пределах шага (StepDataHolder)](#интерфейс-хранения-данных-в-пределах-шага-stepdataholder)
- [Система навигации](#система-навигации)
- [Система состояний и cookies](#система-состояний-и-cookies)
- [Процессоры обработки](#процессоры-обработки)
- [Создание шагов бота](#создание-шагов-бота)
- [Глобальный перехватчик ошибок и кастомизация](#глобальный-перехватчик-ошибок-и-кастомизация)
  - [Глобальный обработчик ошибок](#глобальный-обработчик-ошибок)
  - [Кастомизация текста ошибки](#кастомизация-текста-ошибки)
  - [Логирование](#логирование)
- [Создание бота через фабрику](#создание-бота-через-фабрику)
  - [Конфигурация (DeepStateBotConfig)](#конфигурация-deepstatebotconfig)
  - [Фабрика создания бота](#фабрика-создания-бота)
  - [Репозиторий пользователей](#репозиторий-пользователей)
- [Преимущества системы](#преимущества-системы)

## Общая архитектура

**dsbotmaker** - это Kotlin-фреймворк для создания Telegram ботов с пошаговой навигацией и состоянием пользователя. Проект использует библиотеку `telegrambots` версии 6.8.0 и построен на концепции "шагов" (steps) с автоматической навигацией между ними.

## Инстанс бота (DeepStateBotInstance)

**`DeepStateBotInstance`** - основной класс для работы с ботом после его создания через фабрику. Предоставляет доступ к управлению шагами пользователя, работе с cookies и отправке сообщений.

### Создание инстанса

```kotlin
class DeepStateBotInstance(
    private val stepHandler: StepHandler,
    val sender: TelegramBotSender
)
```

### Основные методы инстанса

#### Управление шагами

**`forceChangeStep`** - принудительное изменение шага пользователя с отправкой сообщения:
```kotlin
suspend fun forceChangeStep(
    userChatId: Long,
    stepType: String,
    errorMsg: String? = null
)
```

**Параметры:**
- `userChatId` - ID чата пользователя
- `stepType` - тип нового шага
- `errorMsg` - опциональное сообщение об ошибке, которое будет отправлено перед переходом

**Пример использования:**
```kotlin
// Переход на главное меню
botInstance.forceChangeStep(userChatId, "main_menu")

// Переход с сообщением об ошибке
botInstance.forceChangeStep(userChatId, "login_step", "Неверный пароль, попробуйте снова")
```

#### Работа с cookies (данными состояния)

**`saveCookie`** - сохранение данных в cookies пользователя:
```kotlin
fun <T: Serializable> saveCookie(
    userChatId: Long, 
    vararg pair: Pair<CookieKey<T>, T?>
)
```

**`getCookie`** - получение данных из cookies пользователя:
```kotlin
fun <T : Serializable> getCookie(
    userChatId: Long, 
    cookieKey: CookieKey<T>
): T?
```

**Пример использования:**
```kotlin
// Сохранение данных пользователя
botInstance.saveCookie(
    userChatId,
    CookieKey(String::class.java, "user_name") to "Иван Иванов",
    CookieKey(Int::class.java, "user_age") to 25,
    CookieKey(Boolean::class.java, "is_premium") to true
)

// Получение данных
val userName = botInstance.getCookie(userChatId, CookieKey(String::class.java, "user_name"))
val userAge = botInstance.getCookie(userChatId, CookieKey(Int::class.java, "user_age"))
```

### Отправка сообщений через TelegramBotSender

Инстанс бота предоставляет доступ к `sender` для отправки различных типов сообщений:

#### Базовые методы отправки

**`sendHtmlMessage`** - отправка HTML-сообщения:
```kotlin
suspend fun sendHtmlMessage(
    chatId: Long,
    textBody: String,
    replyKeyboard: ReplyKeyboard?,
    notify: Boolean = true
): Message?
```

**`editHtmlMessage`** - редактирование существующего сообщения:
```kotlin
suspend fun editHtmlMessage(
    chatId: Long, 
    messageId: Int, 
    textBody: String, 
    replyKeyboard: InlineKeyboardMarkup?
)
```

**`sendStepMessage`** - отправка сообщения шага с автоматическим созданием клавиатуры:
```kotlin
suspend fun sendStepMessage(
    userChatId: Long,
    stepType: String,
    errorMsg: String? = null
)

suspend fun sendStepMessage(
    userChatId: Long,
    step: TelegramBotStep,
    errorMsg: String? = null
)
```

**`editStepMessage`** - редактирование сообщения шага:
```kotlin
suspend fun editStepMessage(
    chatId: Long,
    messageId: Int,
    step: TelegramBotStep,
    removeInlineKeyboard: Boolean
)
```

#### Отправка медиа-контента

**`execute(SendPhoto)`** - отправка фото:
```kotlin
fun execute(method: SendPhoto): Message
```

**`execute(SendVideo)`** - отправка видео:
```kotlin
fun execute(method: SendVideo): Message
```

**`execute(SendDocument)`** - отправка документа:
```kotlin
fun execute(method: SendDocument): Message
```

**`execute(SendVoice)`** - отправка голосового сообщения:
```kotlin
fun execute(method: SendVoice): Message
```

**`execute(SendMediaGroup)`** - отправка группы медиа:
```kotlin
fun execute(method: SendMediaGroup): List<Message>
```

#### Работа с группами

**`getChatMemberCount`** - получение количества участников чата:
```kotlin
suspend fun getChatMemberCount(groupChatId: Long): Int
```

### Практические примеры использования

#### Пример 1: Управление пользовательской сессией
```kotlin
class UserSessionManager(private val botInstance: DeepStateBotInstance) {
    
    suspend fun startUserSession(userChatId: Long) {
        // Сохраняем время начала сессии
        botInstance.saveCookie(
            userChatId,
            CookieKey(Long::class.java, "session_start") to System.currentTimeMillis()
        )
        
        // Переходим на главное меню
        botInstance.forceChangeStep(userChatId, "main_menu")
    }
    
    suspend fun endUserSession(userChatId: Long) {
        // Очищаем сессионные данные
        botInstance.saveCookie(
            userChatId,
            CookieKey(Long::class.java, "session_start") to null,
            CookieKey(String::class.java, "current_action") to null
        )
        
        // Переходим на шаг завершения
        botInstance.forceChangeStep(userChatId, "session_end")
    }
}
```

#### Пример 2: Работа с формами ввода
```kotlin
class RegistrationForm(private val botInstance: DeepStateBotInstance) {
    
    suspend fun processRegistrationStep(userChatId: Long, input: String) {
        val currentStep = botInstance.getCookie(userChatId, CookieKey(String::class.java, "reg_step"))
        
        when (currentStep) {
            "name" -> {
                botInstance.saveCookie(userChatId, CookieKey(String::class.java, "user_name") to input)
                botInstance.saveCookie(userChatId, CookieKey(String::class.java, "reg_step") to "email")
                botInstance.forceChangeStep(userChatId, "input_email")
            }
            "email" -> {
                botInstance.saveCookie(userChatId, CookieKey(String::class.java, "user_email") to input)
                botInstance.saveCookie(userChatId, CookieKey(String::class.java, "reg_step") to null)
                botInstance.forceChangeStep(userChatId, "registration_complete")
            }
        }
    }
}
```

#### Пример 3: Отправка уведомлений
```kotlin
class NotificationService(private val botInstance: DeepStateBotInstance) {
    
    suspend fun sendNotification(userChatId: Long, message: String, isImportant: Boolean = false) {
        try {
            botInstance.sender.sendHtmlMessage(
                chatId = userChatId,
                textBody = if (isImportant) "🔔 <b>$message</b>" else message,
                replyKeyboard = null,
                notify = isImportant
            )
        } catch (e: TelegramApiException) {
            // Логирование ошибки отправки
            println("Не удалось отправить уведомление пользователю $userChatId: ${e.message}")
        }
    }
    
    suspend fun sendBroadcast(message: String, userIds: List<Long>) {
        userIds.forEach { userId ->
            sendNotification(userId, message)
            // Задержка для избежания лимитов Telegram
            delay(100)
        }
    }
}
```

### Обработка ошибок

Все методы отправки сообщений могут выбрасывать `TelegramApiException`:

```kotlin
try {
    botInstance.forceChangeStep(userChatId, "main_menu")
} catch (e: TelegramApiException) {
    when {
        e.message?.contains("bot was blocked") == true -> {
            // Пользователь заблокировал бота
            println("Пользователь $userChatId заблокировал бота")
        }
        e.message?.contains("chat not found") == true -> {
            // Чат не найден
            println("Чат $userChatId не найден")
        }
        else -> {
            // Другие ошибки
            println("Ошибка Telegram API: ${e.message}")
        }
    }
}
```

## Ключевые интерфейсы и маркеры

### Основные интерфейсы шагов (`TelegramBotStep`)

**Базовый интерфейс:**
- `getType(): String` - уникальный идентификатор шага
- `getBody(userChatId: Long): String` - текст сообщения для пользователя
- `nextStepVariantTypes: Set<String>` - возможные следующие шаги

**Маркеры поддержки функциональности:**

#### Интерфейсы для кнопок

- **`ButtonsSupported`** - поддержка обычных кнопок клавиатуры
  - `getButtons(userChatId: Long): List<KeyboardRow>`
  - `getInputPlaceholder(userChatId: Long): String?`

- **`InlineButtonsSupported`** - поддержка инлайн-кнопок
  - `getButtons(userChatId: Long): List<List<InlineKeyboardButton>>`
  - `getNextStepButtons(userChatId: Long): List<List<InlineKeyboardButton>>`
  - `onCallbackDataReceived(callbackQuery: CallbackQuery)`
  - `onCallbackNextStepReceived(callbackQuery: CallbackQuery): Boolean`

- **`ColdActionInlineButtonsSupported`** - специальные "холодные" действия (например, авторизация Avito)
  - `getColdAction(): ColdAction`
  - `getColdActionButtons(userChatId: Long): List<List<HoldActionInlineKeyboardButtonWrapper>>`

#### Интерфейсы обработки сообщений

- **`MessageReceiver`** - получение текстовых сообщений
  - `onMessageReceived(message: Message)` - обработка входящего сообщения
  - `getInputPlaceholder(userChatId: Long): String?` - плейсхолдер для поля ввода

- **`RouteSupported`** - навигация по сообщениям
  - `getNextStep(userChatId: Long, message: String): String` - определение следующего шага на основе текста сообщения
  - **Логика работы**: При вводе текста пользователем система вызывает эту функцию для определения, на какой шаг перейти. Можно использовать как для обычных кнопок, так и для инлайн-кнопок.

#### Интерфейсы навигации и поведения

- **`NoSupportBackButton`** / **`NoSupportInlineBackButton`** - отключение кнопки "Назад"
  - Убирает автоматическую кнопку "Вернуться назад" из навигации

- **`NoSupportNavigateButtons`** / **`NoSupportInlineNavigateButtons`** - отключение всех навигационных кнопок
  - Полностью убирает автоматические навигационные кнопки

- **`MoveBackOnNext`** - автоматический возврат на предыдущий шаг
  - После выполнения шага автоматически возвращает пользователя на предыдущий шаг

- **`SendMessageBeforeNext`** - отправка сообщения перед переходом
  - `getBeforeNextMessage(userChatId: Long): String` - сообщение, которое отправляется перед переходом на следующий шаг

- **`StayOnCurrentStep`** - оставаться на текущем шаге
  - **Логика работы**: Пользователь остается на текущем шаге после ввода сообщения
  - `doNextStep(userChatId: Long): Boolean` - определяет, нужно ли переходить на следующий шаг
  - **Пример использования**: формы ввода, где нужно несколько раз вводить данные

- **`RedirectToAnotherStep`** - перенаправление на другой шаг
  - `getAnotherStep(userChatId: Long): String?` - возвращает тип шага для перенаправления
  - Используется для условной навигации

- **`CanBeSkipped`** / **`CanBeSkippedInBackStep`** - возможность пропуска шага
  - `skipStep(userChatId: Long): Boolean` - определяет, можно ли пропустить шаг при переходе вперед
  - `skipBackStep(userChatId: Long): Boolean` - определяет, можно ли пропустить шаг при переходе назад

#### Дополнительные интерфейсы

- **`ChatMemberUpdatesSupported`** - обработка обновлений участников чата

### Интерфейс хранения данных в пределах шага (`StepDataHolder`)

**`StepDataHolder<T : Any>`** - механизм для хранения и переиспользования данных в пределах одного выполнения шага:

**Для чего нужен:**
- Экономия запросов к БД - данные загружаются один раз и переиспользуются во всех методах шага
- Удобство разработки - доступ к данных из любых методов шага без повторных запросов
- Автоматическое управление жизненным циклом - данные очищаются после отправки сообщения

**Ключевые методы:**
- `getStepData(userChatId: Long): T` - получение данных (автоматически создает при первом вызове)
- `createStepData(userChatId: Long): T` - фабричный метод для инициализации данных
- `setStepData(userChatId: Long, data: T)` - установка данных
- `clearStepData(userChatId: Long)` - очистка данных (default реализация)
- `updateStepData(userChatId: Long, update: T.() -> Unit)` - удобное обновление данных

**Пример использования:**
```kotlin
class UserProfileStep : TelegramBotStep, TelegramBotStep.ButtonsSupported,
    TelegramBotStep.StepDataHolder<UserProfileData> {

    override val dataStorage = mutableMapOf<Long, UserProfileData>()

    override fun createStepData(userChatId: Long): UserProfileData {
        return UserProfileData().apply {
            // Загружаем данные из БД один раз при создании
            user = userRepository.findById(userChatId)
            statistics = statisticsService.getUserStats(userChatId)
        }
    }

    override fun getBody(userChatId: Long): String {
        val data = getStepData(userChatId) // Данные уже загружены
        return """
            👤 <b>Профиль пользователя</b>
            
            Имя: ${data.user.name}
            Статистика: ${data.statistics.totalMessages} сообщений
        """.trimIndent()
    }

    override fun getButtons(userChatId: Long): List<KeyboardRow> {
        val data = getStepData(userChatId) // Используем те же данные без повторного запроса
        return listOf(
            KeyboardRow().apply {
                add("📊 Статистика (${data.statistics.totalMessages})")
            }
        )
    }

    // Остальные методы шага...
}

data class UserProfileData(
    var user: User? = null,
    var statistics: UserStatistics? = null
)
```

**Преимущества:**
- **Автоматическая инициализация** - данные создаются при первом обращении
- **Default методы** - минимальный boilerplate код
- **Типобезопасность** - каждый шаг может иметь свой тип данных
- **Автоматическая очистка** - данные очищаются после отправки сообщения

## Система навигации

**Навигатор (`Navigator`)** управляет:
- Автоматическими кнопками "На главную" и "Вернуться назад"
- Определением предыдущего шага через анализ `nextStepVariantTypes`
- Обработкой навигационных callback'ов

**Константы навигации:**
- `NAV_HOME_BUTTON = "На главную"`
- `NAV_BACK_BUTTON = "Вернуться назад"`
- `NAV_HOME_BUTTON_CALLBACK = "nav_callback:home"`
- `NAV_BACK_BUTTON_CALLBACK = "nav_callback:back"`

## Система состояний и cookies

**`StepHandler`** управляет:
- Текущим шагом пользователя (`getUserStep`, `updateStep`)
- Сохранением состояния через cookies (`saveCookie`, `getCookie`)

**`CookieKey<T>`** - типизированная система хранения данных пользователя:
- Поддержка String, Int, Boolean, Long, Float
- Автоматическая сериализация/десериализация

## Процессоры обработки

Цепочка процессоров обрабатывает сообщения последовательно:
1. **`UserIdFetchProcessor`** - извлечение ID пользователя
2. **`CurrentStepFetchProcessor`** - получение текущего шага
3. **`MessageProcessor`** - обработка текстовых сообщений
4. **`InlineButtonsProcessor`** - обработка инлайн-кнопок
5. **`GetNextStepProcessor`** - определение следующего шага
6. **`NavButtonProcessor`** - обработка навигационных кнопок

## Создание шагов бота

### Наследование от TelegramBotStep
Каждый шаг должен наследоваться от базового интерфейса `TelegramBotStep` и реализовывать необходимые маркеры. Каждый шаг должен иметь уникальное имя (тип).

**Пример создания шага с кнопками:**
```kotlin
class MainMenuStep : TelegramBotStep.ButtonsSupported {
    override fun getType(): String = "main_menu"
    
    override fun getBody(userChatId: Long): String = "Добро пожаловать! Выберите действие:"
    
    override fun getButtons(userChatId: Long): List<KeyboardRow> = listOf(
        KeyboardRow().apply {
            add("Профиль")
            add("Настройки")
        }
    )
    
    override fun getInputPlaceholder(userChatId: Long): String? = null
    
    override val nextStepVariantTypes: Set<String> = setOf("profile", "settings")
}
```

**Пример шага с инлайн-кнопками:**
```kotlin
class ProfileStep : TelegramBotStep.InlineButtonsSupported {
    override fun getType(): String = "profile"
    
    override fun getBody(userChatId: Long): String = "Ваш профиль:"
    
    override fun getButtons(userChatId: Long): List<List<InlineKeyboardButton>> = listOf(
        listOf(
            BotMakerUtil.newInlineBtn("Изменить имя", "change_name"),
            BotMakerUtil.newInlineBtn("Назад", "back")
        )
    )
    
    override fun getNextStepButtons(userChatId: Long): List<List<InlineKeyboardButton>> = emptyList()
    
    override fun onCallbackDataReceived(callbackQuery: CallbackQuery) {
        when (callbackQuery.data) {
            "change_name" -> {
                // Обработка изменения имени
            }
        }
    }
    
    override fun onCallbackNextStepReceived(callbackQuery: CallbackQuery): Boolean = false
    
    override val nextStepVariantTypes: Set<String> = setOf("main_menu")
}
```

**Пример использования RouteSupported для навигации по сообщениям:**
```kotlin
class InputStep : TelegramBotStep.RouteSupported, TelegramBotStep.MessageReceiver {
    override fun getType(): String = "input_step"
    
    override fun getBody(userChatId: Long): String = "Введите ваш возраст:"
    
    override fun getNextStep(userChatId: Long, message: String): String {
        // Определяем следующий шаг на основе введенного текста
        return when {
            message.toIntOrNull() != null -> "age_confirmation"
            else -> "invalid_input"
        }
    }
    
    override fun onMessageReceived(message: Message) {
        // Обработка введенного сообщения
        val age = message.text.toIntOrNull()
        // Сохраняем возраст в cookies или выполняем другую логику
    }
    
    override val nextStepVariantTypes: Set<String> = setOf("age_confirmation", "invalid_input")
}
```

**Пример использования StayOnCurrentStep для формы ввода:**
```kotlin
class MultiInputStep : TelegramBotStep.StayOnCurrentStep, TelegramBotStep.MessageReceiver {
    private val requiredFields = listOf("имя", "фамилия", "email")
    private val fieldKey = CookieKey(String::class.java, "current_field")
    
    override fun getType(): String = "multi_input"
    
    override fun getBody(userChatId: Long): String {
        val currentField = stepHandler.getCookie(userChatId, fieldKey) ?: requiredFields.first()
        return "Введите ваш $currentField:"
    }
    
    override fun onMessageReceived(message: Message) {
        val userChatId = message.chatId
        val currentField = stepHandler.getCookie(userChatId, fieldKey) ?: requiredFields.first()
        val inputValue = message.text
        
        // Сохраняем введенное значение
        stepHandler.saveCookie(userChatId, CookieKey(String::class.java, currentField) to inputValue)
        
        // Переходим к следующему полю или завершаем ввод
        val currentIndex = requiredFields.indexOf(currentField)
        if (currentIndex < requiredFields.size - 1) {
            stepHandler.saveCookie(userChatId, fieldKey to requiredFields[currentIndex + 1])
        } else {
            // Все поля заполнены
            stepHandler.saveCookie(userChatId, fieldKey to null)
        }
    }
    
    override fun doNextStep(userChatId: Long): Boolean {
        // Переходим на следующий шаг только когда все поля заполнены
        val currentField = stepHandler.getCookie(userChatId, fieldKey)
        return currentField == null
    }
    
    override fun getInputPlaceholder(userChatId: Long): String? = "Введите значение"
    
    override val nextStepVariantTypes: Set<String> = setOf("confirmation")
}
```

## Глобальный перехватчик ошибок и кастомизация

### Глобальный обработчик ошибок

Фреймворк предоставляет опциональный глобальный перехватчик ошибок, который позволяет пользователям полностью кастомизировать обработку исключений.

#### Конфигурация обработчика ошибок

```kotlin
class MyBotConfig : DeepStateBotConfig {
    override val mainStepType: String = "main_menu"
    override val botUsername: String = "my_bot"
    override val botToken: String = "YOUR_BOT_TOKEN"
    
    // Опциональный глобальный обработчик ошибок
    override val globalErrorHandler: ((Throwable, Update, TelegramBotSender) -> Unit)? = 
        { error, update, sender ->
            // Логирование в базу данных
            logger.error("Global error caught: ${error.message}", error)
            
            // Отправка уведомления администратору
            sender.sendHtmlMessage(
                chatId = ADMIN_CHAT_ID,
                textBody = "🚨 <b>Ошибка в боте!</b>\n\n${error.message}",
                replyKeyboard = null
            )
            
            // Дополнительная логика обработки
            // Можно изменить поведение бота, сохранить статистику ошибок и т.д.
        }
    
    // Опциональный кастомный текст ошибки для пользователя
    override val customErrorMessage: String? = 
        "❌ Произошла ошибка. Мы уже работаем над ее исправлением!"
    
    override fun rewriteStepOnDeeplink(tgUserId: Long, message: String): TelegramBotStep? {
        // Обработка глубоких ссылок
        return null
    }
}
```

#### Возможности глобального обработчика

**Что можно делать с ошибкой:**
- Логировать в различные системы (база данных, файлы, внешние сервисы)
- Отправлять уведомления администраторам
- Сохранять статистику ошибок
- Изменять поведение бота при ошибках
- Интегрировать с системами мониторинга
- Выполнять любую другую пользовательскую логику

**Когда вызывается обработчик:**
- При ошибках в обработке обновлений (`onUpdateReceived`)
- При ошибках отправки сообщений (`sendStepMessage`)
- При любых других непойманных исключениях в фреймворке

#### Примеры использования

**Пример 1: Логирование в базу данных**
```kotlin
override val globalErrorHandler = { error: Throwable, update: Update, sender: TelegramBotSender ->
    errorRepository.save(ErrorLog(
        timestamp = System.currentTimeMillis(),
        errorMessage = error.message ?: "Unknown error",
        stackTrace = error.stackTraceToString(),
        userId = extractUserId(update),
        updateType = getUpdateType(update)
    ))
}
```

**Пример 2: Отправка уведомлений в Telegram**
```kotlin
override val globalErrorHandler = { error: Throwable, update: Update, sender: TelegramBotSender ->
    // Отправка уведомления администратору
    sender.sendHtmlMessage(
        chatId = ADMIN_CHAT_ID,
        textBody = """
            🚨 <b>Ошибка в боте!</b>
            
            <b>Тип:</b> ${error::class.simpleName}
            <b>Сообщение:</b> ${error.message}
            <b>Пользователь:</b> ${extractUserId(update)}
            <b>Время:</b> ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date())}
        """.trimIndent(),
        replyKeyboard = null
    )
}
```

**Пример 3: Интеграция с Sentry**
```kotlin
override val globalErrorHandler = { error: Throwable, update: Update, sender: TelegramBotSender ->
    Sentry.captureException(error) { scope ->
        scope.setTag("bot_username", botUsername)
        scope.setTag("user_id", extractUserId(update).toString())
        scope.setContext("update", update.toJson())
    }
}
```

### Кастомизация текста ошибки

Пользователи могут полностью заменить стандартный текст ошибки:

```kotlin
// Стандартный текст (по умолчанию)
"📛Возникла непредвиденная ошибка. Мы уже оповестили разработчиков о ней и вернули Вас в главное меню"

// Кастомный текст
override val customErrorMessage: String? = 
    "❌ Упс! Что-то пошло не так. Мы уже знаем о проблеме и скоро все починим!"
```

### Логирование

Фреймворк предоставляет абстрактный интерфейс `BotLogger`, который пользователь может переопределить для интеграции с любой системой логирования. По умолчанию используется простой логгер с `println` и `printStackTrace`.

**Интерфейс логгера:**
```kotlin
interface BotLogger {
    fun debug(msg: String)
    fun info(msg: String)
    fun warn(msg: String)
    fun error(msg: String)
    fun error(msg: String, throwable: Throwable)
}
```

**Примеры использования:**

**Интеграция с Logback/SLF4J:**
```kotlin
class Slf4jBotLogger : BotLogger {
    private val logger = LoggerFactory.getLogger("DeepStateBotMaker")
    
    override fun debug(msg: String) = logger.debug(msg)
    override fun info(msg: String) = logger.info(msg)
    override fun warn(msg: String) = logger.warn(msg)
    override fun error(msg: String) = logger.error(msg)
    override fun error(msg: String, throwable: Throwable) = logger.error(msg, throwable)
}

// Установка кастомного логгера
BotLogger.setLogger(Slf4jBotLogger())
```

**Интеграция с Log4j2:**
```kotlin
class Log4j2BotLogger : BotLogger {
    private val logger = LogManager.getLogger("DeepStateBotMaker")
    
    override fun debug(msg: String) = logger.debug(msg)
    override fun info(msg: String) = logger.info(msg)
    override fun warn(msg: String) = logger.warn(msg)
    override fun error(msg: String) = logger.error(msg)
    override fun error(msg: String, throwable: Throwable) = logger.error(msg, throwable)
}
```

**Интеграция с системой мониторинга:**
```kotlin
class MonitoringBotLogger : BotLogger {
    override fun debug(msg: String) {
        // Логирование в систему мониторинга
        monitoringService.logDebug(msg)
    }
    
    override fun info(msg: String) {
        monitoringService.logInfo(msg)
    }
    
    override fun warn(msg: String) {
        monitoringService.logWarning(msg)
    }
    
    override fun error(msg: String) {
        monitoringService.logError(msg)
    }
    
    override fun error(msg: String, throwable: Throwable) {
        monitoringService.logError(msg, throwable)
    }
}
```

**Преимущества:**
- **Гибкость**: интеграция с любой системой логирования
- **Простота**: по умолчанию используется простой println
- **Кастомизация**: полный контроль над форматом и местом хранения логов
- **Обратная совместимость**: старый код продолжает работать без изменений

## Создание бота через фабрику

### Конфигурация (`DeepStateBotConfig`)
```kotlin
class MyBotConfig : DeepStateBotConfig {
    override val mainStepType: String = "main_menu"
    override val botUsername: String = "my_bot"
    override val botToken: String = "YOUR_BOT_TOKEN"
    
    override fun rewriteStepOnDeeplink(tgUserId: Long, message: String): TelegramBotStep? {
        // Обработка глубоких ссылок
        return null
    }
}
```

### Фабрика создания бота
```kotlin
val botInstance = DeepStateBotMakerFactory.createInstance(
    config = MyBotConfig(),                    // Конфигурация бота
    repository = MyUserRepository(),           // Репозиторий для хранения состояния
    steps = listOf(                            // Список всех шагов бота
        MainMenuStep(),
        ProfileStep(),
        SettingsStep()
    ),
    onUpdateReceivedDoBefore = { update: Update, sender: TelegramBotSender -> Boolean
        // Дополнительная обработка перед основными процессорами
        // Возвращает true для продолжения обработки, false для остановки
        true
    }
)
```

### Аргументы фабрики создания:

1. **`config: DeepStateBotConfig`** - основная конфигурация бота:
   - `mainStepType` - тип главного шага (начальная точка навигации)
   - `botUsername` - имя бота в Telegram
   - `botToken` - токен бота от BotFather
   - `rewriteStepOnDeeplink` - обработка глубоких ссылок

2. **`repository: TgUserRepository`** - репозиторий для хранения состояния:
   - Сохранение текущего шага пользователя
   - Хранение cookies (данных состояния)
   - Должен реализовывать методы для работы с базой данных

3. **`steps: List<TelegramBotStep>`** - список всех шагов бота:
   - Каждый шаг должен иметь уникальный тип
   - Шаги могут реализовывать разные интерфейсы в зависимости от функциональности
   - Должны быть связаны через `nextStepVariantTypes`

4. **`onUpdateReceivedDoBefore`** - функция предварительной обработки:
   - Выполняется перед основными процессорами
   - Может использоваться для логирования, фильтрации, дополнительной обработки
   - Возвращает `true` для продолжения обработки, `false` для остановки

### Репозиторий пользователей
```kotlin
interface TgUserRepository {
    fun getCurrentStep(userChatId: Long): String?
    fun updateStep(userChatId: Long, stepType: String)
    fun getCookies(userChatId: Long): String?
    fun saveCookies(userChatId: Long, cookies: String)
}
```

## Преимущества системы

1. **Модульность** - каждый шаг независим и реализует только нужные интерфейсы
2. **Автоматическая навигация** - система сама управляет переходами между шагами
3. **Состояние пользователя** - сохранение данных между сессиями
4. **Гибкая обработка** - поддержка разных типов взаимодействия (сообщения, кнопки, callback'ы)
5. **Обработка ошибок** - система исключений для управления потоком

Эта архитектура позволяет создавать сложные боты с многоуровневыми меню, формами и сложной логикой навигации, сохраняя при этом чистоту кода и переиспользуемость компонентов.
