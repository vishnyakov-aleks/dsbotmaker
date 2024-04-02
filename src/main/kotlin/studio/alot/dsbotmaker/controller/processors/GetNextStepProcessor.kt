package studio.alot.avitowheelsparser.presentation.telegram.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.avitowheelsparser.domain.TgStepHandler
import studio.alot.avitowheelsparser.presentation.telegram.Navigator
import studio.alot.avitowheelsparser.presentation.telegram.TelegramBotStep

class GetNextStepProcessor(
    private val navigator: Navigator,
    private val stepHandler: TgStepHandler
) : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        dependency as Processor.Result.CurrentStepResult
        val userChatId = dependency.userChatId
        val currentStep = dependency.currentStep

        return try {
            val nextStep = nextStep(upd, userChatId, currentStep)
            stepHandler.updateStep(userChatId, nextStep.getType())

            println("last upd: $upd")
            Processor.Result.SendStepMessageResult(
                userChatId,
                currentStep,
                nextStep,
                supportMessageBeforeNext = true
            )
        } catch (e: MessageReasonException) {
            Processor.Result.SendErrorMessageResult(
                userChatId,
                currentStep,
                e.reasonMsg,
                returnToMainStep = false
            )
        }


    }

    private fun nextStep(upd: Update, userChatId: Long, currentStep: TelegramBotStep?): TelegramBotStep {
        val allCandidates = navigator.getCandidatesForNextStep(userChatId, currentStep)
            ?: return currentStep!!

        val candidate = if (allCandidates.size == 1) {
            allCandidates.first()
        } else {
            currentStep as TelegramBotStep.RouteSupported
            val message = if (upd.hasMessage()) {
                upd.message.text
            } else if (upd.hasCallbackQuery()) {
                upd.callbackQuery.data
            } else {
                throw RuntimeException()
            }

            currentStep.getNextStep(userChatId, message)
        }

        if (!allCandidates.contains(candidate)) {
            throw RuntimeException(
                "this step type was not declared in router map. \n" +
                        "Current type: ${currentStep?.getType()}\n" +
                        "Candidate: $candidate"
            )
        }

        val candidateStep = navigator.getStepFromType(candidate)


        return if (candidateStep is TelegramBotStep.CanBeSkipped && candidateStep.skipStep(userChatId)) {
            nextStep(upd, userChatId, candidateStep)
        } else {
            candidateStep
        }
    }

    class MessageReasonException(val reasonMsg: String) : Exception()
}