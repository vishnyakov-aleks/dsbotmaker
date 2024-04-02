package studio.alot.dsbotmaker

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.controller.TGBotController

object DeepStateBotMakerFactory {

    private val instanceList: HashMap<String, DeepStateBotInstance> = hashMapOf()
    fun createInstance(
        deepStateBotConfig: DeepStateBotConfig,
        repository: TgUserRepository,
        steps: List<TelegramBotStep>,
        onUpdateReceivedDoBefore: (update: Update, tgBotSender: TelegramBotSender) -> Boolean,
    ): DeepStateBotInstance {

        val stepHandler = TgStepHandler(repository)
        val tgBot = TGBot(deepStateBotConfig.botUsername, stepHandler, deepStateBotConfig.botToken)
        TGBotController(
            stepHandler,
            navigator = Navigator(steps, deepStateBotConfig),
            tgBot,
            onUpdateReceivedDoBefore,
            deepStateBotConfig,
            ChatMemberHandler(),
            StartMessageHandler(repository)
        )

        return DeepStateBotInstance(stepHandler, tgBot).also {
            instanceList[deepStateBotConfig.botUsername] = it
        }
    }

    fun getInstance(botName: String) = instanceList[botName]
}