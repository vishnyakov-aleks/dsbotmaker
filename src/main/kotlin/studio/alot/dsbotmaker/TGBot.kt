package studio.alot.dsbotmaker

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

internal class TGBot(
    private val botUsername: String,
    private val stepHandler: StepHandler,
    botToken: String,
    private val config: DeepStateBotConfig
) : TelegramLongPollingBot(botToken),
    CoroutineScope, TelegramBotSender {

    override val coroutineContext = CoroutineName("TGBot")

    private lateinit var updateReceivedCallback: (Update) -> Unit

    private lateinit var navigator: Navigator

    override fun getBotUsername(): String {
        return botUsername
    }

    override fun navigator(): Navigator {
        return navigator
    }

    fun init(navigator: Navigator, callback: (Update) -> Unit) {
        this.navigator = navigator
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        telegramBotsApi.registerBot(this)
        try {
            updateReceivedCallback = callback
        } catch (ex: Throwable) {
            //todo передать Payload в делегирование
            BotLogger.error("Error during bot initialization", ex)
        }
    }

    override fun onUpdateReceived(upd: Update) {
        try {
            updateReceivedCallback(upd)
        } catch (throwable: Throwable) {
            // Вызов глобального обработчика ошибок, если он предоставлен
            config.globalErrorHandler?.invoke(throwable, upd, this)
            
            BotLogger.error("Error during update processing", throwable)
            val userId = if (upd.hasMessage()) {
                upd.message.chatId
            } else if (upd.hasCallbackQuery()) {
                upd.callbackQuery.from.id
            } else {
                return
            }

            runBlocking {
                stepHandler.updateStep(userId, navigator.mainStepType)
                val errorMessage = config.customErrorMessage ?: 
                    "📛Возникла непредвиденная ошибка. Мы уже оповестили разработчиков о ней и вернули Вас в главное меню"
                sendStepMessage(
                    userId,
                    navigator.mainStepType,
                    errorMessage
                )
            }

        }
    }

    override suspend fun sendStepMessage(userChatId: Long, stepType: String, errorMsg: String?) {
        try {
            super.sendStepMessage(userChatId, stepType, errorMsg)
        } catch (t: Throwable) {
            // Вызов глобального обработчика ошибок, если он предоставлен
            config.globalErrorHandler?.invoke(t, Update(), this)
            
            BotLogger.error("Error sending step message by type", t)
            stepHandler.updateStep(userChatId, navigator.mainStepType)
            if (errorMsg == null) {
                val errorMessage = config.customErrorMessage ?: 
                    "📛Возникла непредвиденная ошибка. Мы уже оповестили разработчиков о ней и вернули Вас в главное меню"
                sendStepMessage(
                    userChatId, navigator().mainStepType,
                    errorMessage
                )
            }
        }
    }

    override suspend fun sendStepMessage(userChatId: Long, step: TelegramBotStep, errorMsg: String?) {
        try {
            super.sendStepMessage(userChatId, step, errorMsg)
        } catch (t: Throwable) {
            // Вызов глобального обработчика ошибок, если он предоставлен
            config.globalErrorHandler?.invoke(t, Update(), this)
            
            BotLogger.error("Error sending step message by step object", t)
            stepHandler.updateStep(userChatId, navigator.mainStepType)
            if (errorMsg == null) {
                val errorMessage = config.customErrorMessage ?: 
                    "📛Возникла непредвиденная ошибка. Мы уже оповестили разработчиков о ней и вернули Вас в главное меню"
                sendStepMessage(
                    userChatId, navigator().mainStepType,
                    errorMessage
                )
            }
        }
    }
}
