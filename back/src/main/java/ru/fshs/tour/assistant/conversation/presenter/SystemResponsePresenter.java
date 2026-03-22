package ru.fshs.tour.assistant.conversation.presenter;

import ru.fshs.tour.assistant.conversation.presenter.dto.HumanReadableReply;
import ru.fshs.tour.assistant.conversation.presenter.dto.PresentationInput;

/**
 * Converts internal structured responses to user-facing text.
 */
public interface SystemResponsePresenter {

    HumanReadableReply present(PresentationInput input);
}