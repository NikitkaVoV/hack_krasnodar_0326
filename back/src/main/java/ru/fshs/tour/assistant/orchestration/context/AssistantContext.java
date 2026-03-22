package ru.fshs.tour.assistant.orchestration.context;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Runtime assistant context assembled per request.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantContext {

    private UUID userId;
    private UserProfileSnapshot userProfile;
    private UUID activeRouteId;
    private UUID currentPlaceId;
    private UUID currentEventId;
}