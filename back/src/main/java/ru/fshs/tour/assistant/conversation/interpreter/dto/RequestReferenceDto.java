package ru.fshs.tour.assistant.conversation.interpreter.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestReferenceDto {

    private UUID routeId;
    private UUID placeId;
    private UUID eventId;
}