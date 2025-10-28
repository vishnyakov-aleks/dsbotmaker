package studio.alot.dsbotmaker.controller

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.*
import studio.alot.dsbotmaker.controller.processors.*


internal class TGBotController(
    private val stepHandler: TgStepHandler,
    private val navigator: Navigator,
    private val tgBot: TGBot,

    onUpdateReceivedDoBefore: (update: Update, tgBotSender: TelegramBotSender) -> Boolean,
    deepStateBotConfig: DeepStateBotConfig,
    chatMemberHandler: ChatMemberHandler?,
    botStartMessageHandler: StartMessageHandler,
) : CoroutineScope {

    override val coroutineContext = CoroutineName("TGBot")

    init {
        tgBot.init(navigator) { this.onUpdateReceived(it) }
    }

    private val script: List<Processor> = listOf(
        DoBeforeProcessor { onUpdateReceivedDoBefore(it, tgBot) },
        ChatLogProcessor(),
        ChatMemberHandlerProcessor(chatMemberHandler),
        UserIdFetchProcessor(),
        StartChatProcessor(
            navigator.getStepFromString(navigator.mainStepType),
            botStartMessageHandler,
            deepStateBotConfig
        ),
        CurrentStepFetchProcessor(navigator, stepHandler),
        StepDataProcessor(), // Инициализация данных шага перед вызовом других методов
        NavButtonProcessor(navigator, stepHandler),
        StepToRedirectProcessor(navigator, stepHandler),
        MessageProcessor(),
        InlineButtonsProcessor(),
        ChatMemberUpdatesProcessor(),
        StayOnThisStepProcessor(),
        GetNextStepProcessor(navigator, stepHandler)
    )

    private fun onUpdateReceived(upd: Update) {
        var result: Processor.Result = Processor.Result.ContinueEmptyProcessingResult
        script.forEach {
            result = it.process(upd, result)
//            println("Processor: ${it::class.simpleName}. Result after: ${result::class.simpleName}")
            when (result) {
                is Processor.Result.ExitProcessingResult -> return
                is Processor.Result.SendStepMessageResult -> {
                    sendStepMessage(upd, result as Processor.Result.SendStepMessageResult)
                    return
                }

                is Processor.Result.EditInlineButtonsMessageResult -> {
                    editInlineButtonsMessage(upd, result as Processor.Result.EditInlineButtonsMessageResult)
                    return
                }

                is Processor.Result.SendErrorMessageResult -> {
                    resendStepMessageWithError(upd, result as Processor.Result.SendErrorMessageResult)
                    return
                }

                else -> return@forEach
            }
        }
    }

    private fun editInlineButtonsMessage(
        upd: Update,
        result: Processor.Result.EditInlineButtonsMessageResult,
    ) = launch(IO) {
        tgBot.editStepMessage(
            result.userChatId,
            upd.callbackQuery.message.messageId,
            result.currentStep,
            false
        )
    }

    private fun resendStepMessageWithError(
        upd: Update,
        result: Processor.Result.SendErrorMessageResult,
    ) = launch(IO) {
        val userChatId = result.userChatId

        if (upd.hasCallbackQuery() &&
            result.currentStep is TelegramBotStep.InlineButtonsSupported
        ) {
            tgBot.editStepMessage(
                userChatId,
                upd.callbackQuery.message.messageId,
                result.currentStep,
                removeInlineKeyboard = true
            )
        }

        val step = if (result.returnToMainStep) {
            stepHandler.updateStep(result.userChatId, navigator.mainStepType)
            navigator.getStepFromString(navigator.mainStepType)
        } else {
            result.currentStep
        }

        tgBot.sendStepMessage(
            userChatId,
            step,
            result.errorMsg
        )
    }

    private fun sendStepMessage(upd: Update, result: Processor.Result.SendStepMessageResult) = launch(IO) {
        val currentStep = result.currentStep
        val userChatId = result.userChatId

        if (upd.hasCallbackQuery() && currentStep is TelegramBotStep.InlineButtonsSupported) {
            tgBot.editStepMessage(
                userChatId,
                upd.callbackQuery.message.messageId,
                currentStep,
                removeInlineKeyboard = true
            )
        }

        if (result.supportMessageBeforeNext &&
            currentStep is TelegramBotStep.SendMessageBeforeNext
        ) {
            tgBot.sendHtmlMessage(userChatId, currentStep.getBeforeNextMessage(userChatId), null)
            delay(1000)
        }

        tgBot.sendStepMessage(
            userChatId,
            result.stepToMove
        )
    }
}
