package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.Navigator
import studio.alot.dsbotmaker.TelegramBotStep
import studio.alot.dsbotmaker.TgStepHandler

internal class CurrentStepFetchProcessor(
    private val navigator: Navigator,
    private val stepHandler: TgStepHandler
) : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        dependency as Processor.Result.UserIdResult

        val currentStep = getCurrentStep(upd, dependency.userChatId)

        return Processor.Result.CurrentStepResult(dependency.userChatId, currentStep)
    }


    private fun getCurrentStep(upd: Update, userChatId: Long): TelegramBotStep {
        val result = upd.callbackQuery?.data?.let { data ->
            TelegramBotStep.ColdActionInlineButtonsSupported.ColdAction.entries
                .find {
                    data.contains(TelegramBotStep.ColdActionInlineButtonsSupported.DIVIDER)
                            && data.split(TelegramBotStep.ColdActionInlineButtonsSupported.DIVIDER)
                        .first() == it.ordinal.toString()
                }
        }?.let {
            navigator.getStepFromColdAction(it)
        }?.also { stepHandler.updateStep(userChatId, it.getType()) }
            ?: stepHandler.getUserStep(userChatId)?.let { navigator.getStepFromString(it) }

        return result
            ?: navigator.getStepFromString(navigator.mainStepType)
                .also { stepHandler.updateStep(userChatId, it.getType()) }
    }
}