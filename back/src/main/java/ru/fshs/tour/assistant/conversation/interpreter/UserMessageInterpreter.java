package ru.fshs.tour.assistant.conversation.interpreter;

import ru.fshs.tour.assistant.conversation.interpreter.dto.AssistantInterpretationInput;
import ru.fshs.tour.assistant.conversation.interpreter.dto.NormalizedAssistantRequest;

/**
 * Converts user text into normalized system request.
 */
public interface UserMessageInterpreter {

    NormalizedAssistantRequest interpret(AssistantInterpretationInput input);
}