package studio.alot.avitowheelsparser.presentation.telegram

import studio.alot.avitowheelsparser.data.Step

class DeepStateBotInstance(
    private val stepHandler: StepHandler,
    private val tgBotSender: TelegramBotSender) {
    suspend fun forceChangeStep(
        userChatId: Long,
        step: Step,
        errorMsg: String? = null
    ) {
        stepHandler.updateStep(userChatId, step)
        tgBotSender.sendStepMessage(userChatId, step, errorMsg)
    }


}