package studio.alot.avitowheelsparser.presentation.telegram.controller

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.avitowheelsparser.domain.StartMessageHandler
import studio.alot.avitowheelsparser.domain.TgStepHandler
import studio.alot.avitowheelsparser.presentation.telegram.*
import studio.alot.avitowheelsparser.presentation.telegram.controller.processors.*


abstract class TGBotController(
    private val stepHandler: TgStepHandler,
    private val navigator: Navigator,
    private val tgBot: TGBot,
    chatMemberHandler: ChatMemberHandler?,
    botStartMessageHandler: StartMessageHandler,
) : CoroutineScope {

    override val coroutineContext = CoroutineName("TGBot")

    abstract fun onUpdateReceivedDoBefore(upd: Update): Boolean

    init {
        tgBot.init(navigator) { this.onUpdateReceived(it) }
    }

    private val script: List<Processor> = listOf(
        DoBeforeProcessor { onUpdateReceivedDoBefore(it) },
        ChatLogProcessor(),
        ChatMemberHandlerProcessor(chatMemberHandler),
        UserIdFetchProcessor(),
        StartChatProcessor(botStartMessageHandler),
        CurrentStepFetchProcessor(navigator, stepHandler),
        NavButtonProcessor(navigator, stepHandler),
        StepToRedirectProcessor(navigator, stepHandler),
        InlineButtonsProcessor(),
        MessageProcessor(),
        ChatMemberUpdatesProcessor(),
        StayOnThisStepProcessor(),
        GetNextStepProcessor(navigator, stepHandler)
    )

    private fun onUpdateReceived(upd: Update) {
        var result: Processor.Result = Processor.Result.ContinueEmptyProcessingResult
        script.forEach {
            result = it.process(upd, result)
            println("Processor: ${it::class.simpleName}. Result after: ${result::class.simpleName}")
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
            stepHandler.updateStep(result.userChatId, navigator.mainStep)
            navigator.getStepFromType(navigator.mainStep)
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

        if (currentStep is TelegramBotStep.InlineButtonsSupported) {
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