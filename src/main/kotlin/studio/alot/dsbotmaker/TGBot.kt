package studio.alot.avitowheelsparser.presentation.telegram

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import studio.alot.avitowheelsparser.data.Step


abstract class TGBot : TelegramLongPollingBot(), CoroutineScope, TelegramBotSender {

    @Value("\${telegram.bot.enabled}")
    lateinit var telegramEnabled: String

    override val coroutineContext = CoroutineName("TGBot")

    private lateinit var updateReceivedCallback: (Update) -> Unit

    private lateinit var navigator: Navigator

    override fun navigator(): Navigator {
        return navigator
    }

    fun init(navigator: Navigator, callback: (Update) -> Unit) {
        if (telegramEnabled.toBoolean()) {
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
    }

    override fun onUpdateReceived(upd: Update) {
        try {
            updateReceivedCallback(upd)
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    override suspend fun sendStepMessage(userChatId: Long, step: Step, errorMsg: String?) {
        try {
            super.sendStepMessage(userChatId, step, errorMsg)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    override suspend fun sendStepMessage(userChatId: Long, step: TelegramBotStep, errorMsg: String?) {
        try {
            super.sendStepMessage(userChatId, step, errorMsg)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}