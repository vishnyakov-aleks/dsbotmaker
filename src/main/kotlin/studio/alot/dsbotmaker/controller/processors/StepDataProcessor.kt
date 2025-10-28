package studio.alot.dsbotmaker.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update
import studio.alot.dsbotmaker.TelegramBotStep

internal class StepDataProcessor : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {
        // Проверяем, что у нас есть текущий шаг
        if (dependency !is Processor.Result.CurrentStepResult) {
            return dependency
        }

        val currentStep = dependency.currentStep
        val userChatId = dependency.userChatId

        // Если шаг реализует StepDataHolder, предварительно заполняем данные
        if (currentStep is TelegramBotStep.StepDataHolder<*>) {
            // Вызываем getStepData для инициализации данных
            // Это гарантирует, что данные будут доступны во всех последующих методах
            currentStep.getStepData(userChatId)
        }

        return dependency
    }
}
