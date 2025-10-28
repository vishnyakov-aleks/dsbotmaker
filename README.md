# DSBotMaker - Фреймворк для создания Telegram ботов

## Общая архитектура

**dsbotmaker** - это Kotlin-фреймворк для создания Telegram ботов с пошаговой навигацией и состоянием пользователя. Проект использует библиотеку `telegrambots` версии 6.8.0 и построен на концепции "шагов" (steps) с автоматической навигацией между ними.

## Ключевые интерфейсы и маркеры

### Основные интерфейсы шагов (`TelegramBotStep`)

**Базовый интерфейс:**
- `getType(): String` - уникальный идентификатор шага
- `getBody(userChatId: Long): String` - текст сообщения для пользователя
- `nextStepVariantTypes: Set<String>` - возможные следующие шаги

**Маркеры поддержки функциональности:**

### Интерфейсы для кнопок
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

### Интерфейсы обработки сообщений
- **`MessageReceiver`** - получение текстовых сообщений
  - `onMessageReceived(message: Message)` - обработка входящего сообщения
  - `getInputPlaceholder(userChatId: Long): String?` - плейсхолдер для поля ввода

- **`RouteSupported`** - навигация по сообщениям
  - `getNextStep(userChatId: Long, message: String): String` - определение следующего шага на основе текста сообщения
  - **Логика работы**: При вводе текста пользователем система вызывает эту функцию для определения, на какой шаг перейти. Можно использовать как для обычных кнопок, так и для инлайн-кнопок.

### Интерфейсы навигации и поведения

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

### Дополнительные интерфейсы
- **`ChatMemberUpdatesSupported`** - обработка обновлений участников чата

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
