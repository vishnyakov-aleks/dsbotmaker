package studio.alot.dsbotmaker

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Credentials
import okhttp3.OkHttpClient
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.io.Serializable
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

internal class TGBot(
    private val botUsername: String,
    private val stepHandler: StepHandler,
    val botToken: String,
    private val config: DeepStateBotConfig
) : LongPollingSingleThreadUpdateConsumer,
    CoroutineScope, TelegramBotSender {

    override val coroutineContext = CoroutineName("TGBot")

    private lateinit var updateReceivedCallback: (Update) -> Unit

    private lateinit var navigator: Navigator

    private val telegramClient: TelegramClient = createTelegramClient(botToken)
    private val botApplication: TelegramBotsLongPollingApplication = createBotApplication()

    override fun navigator(): Navigator {
        return navigator
    }

    fun init(navigator: Navigator, callback: (Update) -> Unit) {
        this.navigator = navigator
        try {
            botApplication.registerBot(botToken, this)
            updateReceivedCallback = callback
        } catch (ex: Throwable) {
            BotLogger.error("Error during bot initialization", ex)
        }
    }

    fun start() {
        botApplication.start()
    }

    fun stop() {
        botApplication.stop()
    }

    override fun consume(update: Update) {
        try {
            updateReceivedCallback(update)
        } catch (throwable: Throwable) {
            config.globalErrorHandler?.invoke(throwable, update, this)

            BotLogger.error("Error during update processing", throwable)
            val userId = if (update.hasMessage()) {
                update.message.chatId
            } else if (update.hasCallbackQuery()) {
                update.callbackQuery.from.id
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

    override fun <T : Serializable, Method : BotApiMethod<T>> execute(method: Method): T {
        try {
            return telegramClient.execute(method)
        } catch (e: Exception) {
            throw TelegramApiException("Error executing method", e)
        }
    }

    override fun execute(method: SendVideo): Message {
        return telegramClient.execute(method)
    }

    override fun execute(method: SendPhoto): Message {
        return telegramClient.execute(method)
    }

    override fun execute(method: SendDocument): Message {
        return telegramClient.execute(method)
    }

    override fun execute(method: SendMediaGroup): List<Message> {
        return telegramClient.execute(method)
    }

    override fun execute(method: SendVoice): Message {
        return telegramClient.execute(method)
    }

    override suspend fun sendStepMessage(userChatId: Long, stepType: String, errorMsg: String?) {
        try {
            super.sendStepMessage(userChatId, stepType, errorMsg)
        } catch (t: Throwable) {
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

    private fun createTelegramClient(botToken: String): TelegramClient {
        val okClient = buildOkHttpClient()
        return OkHttpTelegramClient(okClient, botToken)
    }

    private fun buildOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        return if (config.proxyEnabled && !config.proxyHost.isNullOrBlank() && (config.proxyPort ?: 0) > 0) {
            TelegramOkHttpClientFactory.HttpProxyOkHttpClientCreator(
                { Proxy(Proxy.Type.HTTP, InetSocketAddress(config.proxyHost, config.proxyPort!!)) },
                {
                    okhttp3.Authenticator { _, response ->
                        val username = config.proxyUsername ?: ""
                        val password = config.proxyPassword ?: ""
                        if (username.isNotBlank() && password.isNotBlank()) {
                            val credential = Credentials.basic(username, password)
                            response.request.newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build()
                        } else {
                            null
                        }
                    }
                }
            ).get()
        } else {
            builder.build()
        }
    }

    private fun createBotApplication(): TelegramBotsLongPollingApplication {
        val okClient = buildOkHttpClient()
        val objectMapperSupplier = Supplier { com.fasterxml.jackson.databind.ObjectMapper() }
        val okHttpClientSupplier = Supplier { okClient }
        return TelegramBotsLongPollingApplication(objectMapperSupplier, okHttpClientSupplier)
    }
}