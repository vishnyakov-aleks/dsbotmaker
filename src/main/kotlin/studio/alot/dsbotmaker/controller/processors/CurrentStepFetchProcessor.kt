package studio.alot.avitowheelsparser.presentation.telegram.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.avitowheelsparser.domain.TgStepHandler
import studio.alot.avitowheelsparser.presentation.telegram.Navigator
import studio.alot.avitowheelsparser.presentation.telegram.TelegramBotStep

class CurrentStepFetchProcessor(
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
            TelegramBotStep.ColdActionInlineButtonsSupported.ColdAction.values()
                .find {
                    data.contains(TelegramBotStep.ColdActionInlineButtonsSupported.DIVIDER)
                            && data.split(TelegramBotStep.ColdActionInlineButtonsSupported.DIVIDER)
                        .first() == it.ordinal.toString()
                }
        }?.let {
            navigator.getStepFromColdAction(it)
        }?.also { stepHandler.updateStep(userChatId, it.getType()) }
            ?: stepHandler.getUserStep(userChatId)?.let { navigator.getStepFromType(it) }

        return result
            ?: navigator.getStepFromType(navigator.mainStep)
                .also { stepHandler.updateStep(userChatId, it.getType()) }
    }
}