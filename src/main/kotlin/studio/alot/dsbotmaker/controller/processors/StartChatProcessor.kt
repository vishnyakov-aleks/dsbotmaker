package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.DeepStateBotConfig
import studio.alot.dsbotmaker.StartMessageHandler
import studio.alot.dsbotmaker.TelegramBotStep

internal class StartChatProcessor(
    private val mainStep: TelegramBotStep,
    private val botStartMessageHandler: StartMessageHandler,
    private val deepStateBotConfig: DeepStateBotConfig,
) : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
       return if (upd.message?.text?.startsWith("/start") == true) {
            botStartMessageHandler.processStartCommand(deepStateBotConfig.mainStepType, upd.message)
            Processor.Result.SendStepMessageResult(upd.message.from.id, mainStep, mainStep, false)
        } else {
            dependency
       }
    }
}