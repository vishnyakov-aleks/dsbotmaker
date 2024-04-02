package studio.alot.dsbotmaker.exceptions

class OnInlineButtonsMessageReasonException(val returnToMainStep: Boolean, val reason: String) : Exception()
