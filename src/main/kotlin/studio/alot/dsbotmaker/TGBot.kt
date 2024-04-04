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
    botToken: String
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
            ex.printStackTrace()
        }
    }

    override fun onUpdateReceived(upd: Update) {
        try {
            updateReceivedCallback(upd)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            val userId = if (upd.hasMessage()) {
                upd.message.chatId
            } else if (upd.hasCallbackQuery()) {
                upd.callbackQuery.from.id
            } else {
                return
            }

            runBlocking {
                stepHandler.updateStep(userId, navigator.mainStepType)
                sendStepMessage(
                    userId,
                    navigator.mainStepType,
                    "📛Возникла непредвиденная ошибка. Мы уже оповестили разработчиков о ней и вернули Вас в главное меню"
                )
            }

        }
    }

    override suspend fun sendStepMessage(userChatId: Long, stepType: String, errorMsg: String?) {
        try {
            super.sendStepMessage(userChatId, stepType, errorMsg)
        } catch (t: Throwable) {
            t.printStackTrace()
            stepHandler.updateStep(userChatId, navigator.mainStepType)
            if (errorMsg == null) {
                sendStepMessage(
                    userChatId, navigator().mainStepType,
                    "📛Возникла непредвиденная ошибка. Мы уже оповестили разработчиков о ней и вернули Вас в главное меню"
                )
            }
        }
    }

    override suspend fun sendStepMessage(userChatId: Long, step: TelegramBotStep, errorMsg: String?) {
        try {
            super.sendStepMessage(userChatId, step, errorMsg)
        } catch (t: Throwable) {
            t.printStackTrace()
            stepHandler.updateStep(userChatId, navigator.mainStepType)
                       if (errorMsg == null) {
 sendStepMessage(
                userChatId, navigator().mainStepType,
                "📛Возникла непредвиденная ошибка. Мы уже оповестили разработчиков о ней и вернули Вас в главное меню"
            )
                       }
        }
    }
}
