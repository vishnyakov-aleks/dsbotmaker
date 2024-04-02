package studio.alot.dsbotmaker



interface StepHandler {

    fun getUserStep(userChatId: Long): StepType?
    fun updateStep(userChatId: Long, stepType: StepType)
}