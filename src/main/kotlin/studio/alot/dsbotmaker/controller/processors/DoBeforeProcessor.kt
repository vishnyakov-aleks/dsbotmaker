package studio.alot.avitowheelsparser.presentation.telegram.controller.processors

import org.telegram.telegrambots.meta.api.objects.Update

class DoBeforeProcessor(private val updateBeforeFun: (upd: Update) -> Boolean) : Processor {
    override fun process(upd: Update, dependency: Processor.Result): Processor.Result {

        return if (updateBeforeFun(upd))
            Processor.Result.ExitProcessingResult
        else
            Processor.Result.ContinueEmptyProcessingResult
    }
}